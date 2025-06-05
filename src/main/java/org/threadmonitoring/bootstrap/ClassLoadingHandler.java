package org.threadmonitoring.bootstrap;

import net.bytebuddy.dynamic.loading.ClassInjector;
import org.threadmonitoring.ThreadAgent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;

public class ClassLoadingHandler {

    private static final String BOOTSTRAP_AGENT_CLASS = "org.threadmonitoring.bootstrap.Bootstrapper";

    public static Method handleClassLoading() {
        byte[] classBytes;
        try {
            classBytes = loadBootstrapAgentBytes();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        ClassInjector.UsingUnsafe.ofBootLoader().injectRaw(Collections.singletonMap(BOOTSTRAP_AGENT_CLASS, classBytes));
        Class<?> bootstrapAgentClass;

        try {
            bootstrapAgentClass = Class.forName(BOOTSTRAP_AGENT_CLASS, false, null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        Method bootstrap;

        try {

            MethodHandle bootstrapMethod = MethodHandles.lookup().findStatic(ClassLoadingHandler.class,
                    "bootstrap", MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class,
                            MethodType.class, String.class, Object[].class));

            Method setBootstrap = bootstrapAgentClass.getDeclaredMethod("setBootstrap", MethodHandle.class);
            setBootstrap.invoke(null, bootstrapMethod);
            bootstrap = bootstrapAgentClass.getDeclaredMethod("bootstrap", MethodHandles.Lookup.class,
                    String.class, MethodType.class, String.class, Object[].class);

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException(e);

        }
        return bootstrap;
    }

    public static CallSite bootstrap(MethodHandles.Lookup sourceMethodLookup, String adviceMethodName, MethodType adviceMethodType, String adviceClassName, Object[] args) {
        try {
            ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(ThreadAgent.class.getClassLoader());
            Class<?> adviceClass = Class.forName(adviceClassName);
            Thread.currentThread().setContextClassLoader(currentClassLoader);
            MethodHandle aStatic = MethodHandles.lookup().findStatic(adviceClass, adviceMethodName, adviceMethodType);
            return new ConstantCallSite(aStatic);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static byte[] loadBootstrapAgentBytes() throws IOException {
        byte[] classBytes;
        try (InputStream in = ThreadAgent.class.getClassLoader().getResourceAsStream(BOOTSTRAP_AGENT_CLASS.replace('.', '/').concat(".class"))) {
            if (in != null) {
                classBytes = inputStreamToBytes(in);
            } else {
                System.out.println("Classloader resource not found org.threadmonitoring.bootstrap.Bootstrapper");
                throw new IOException("Classloader resource not found org.threadmonitoring.bootstrap.Bootstrapper");
            }
            return classBytes;
        }
    }

    private static byte[] inputStreamToBytes(InputStream in) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4 * 0x400];
            int length;
            while ((length = in.read(buffer)) != -1) {
                os.write(buffer, 0, length);
            }
            return os.toByteArray();
        }
    }

}
