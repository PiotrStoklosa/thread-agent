package org.threadmonitoring;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.matcher.ElementMatchers;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.threadmonitoring.advices.*;
// import org.threadmonitoring.connection.Publisher;
import org.threadmonitoring.scanner.ThreadStatusScanner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.invoke.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.jar.JarFile;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class Main {

    private static final String BOOTSTRAP_AGENT_CLASS = "org.threadmonitoring.bootstrap.BootstrapAgent";

    //public static final Logger logger = LogManager.getLogger(ThreadConstructorAdvice.class);

    public static void run(Instrumentation inst) throws IOException, ClassNotFoundException {

        byte[] classBytes = loadBootstrapAgentBytes();
        ClassInjector.UsingUnsafe.ofBootLoader().injectRaw(Collections.singletonMap(BOOTSTRAP_AGENT_CLASS, classBytes));

        Class<?> bootstrapAgentClass = Class.forName(BOOTSTRAP_AGENT_CLASS, false, null);
        Method bootstrap;
        try {
            MethodHandle bootstrapMethod = MethodHandles.lookup().findStatic(Main.class, "bootstrap", MethodType.methodType(CallSite.class,
                    MethodHandles.Lookup.class,
                    String.class,
                    MethodType.class,
                    String.class,
                    Object[].class));
            Method setBootstrap = bootstrapAgentClass.getDeclaredMethod("setBootstrap", MethodHandle.class);
            setBootstrap.invoke(null, bootstrapMethod);
            bootstrap = bootstrapAgentClass.getDeclaredMethod("bootstrap", MethodHandles.Lookup.class,
                    String.class,
                    MethodType.class,
                    String.class,
                    Object[].class);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
 // zmienic na jeden Agent Builder


/*        new AgentBuilder
                .Default()
                .ignore(none())
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .disableClassFormatChanges()
                .type(ElementMatchers.is(Thread.class))
                .transform((builder, typeDescription, classLoader, module, protectionDomain)
                        -> builder
                        .visit(Advice.to(ThreadConstructorAdvice.class)
                                .on(ElementMatchers.isConstructor()))).installOn(inst);*/

        AgentBuilder agentBuilder = new AgentBuilder
                .Default()
                .ignore(none())
                .disableClassFormatChanges()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(AgentBuilder.RedefinitionStrategy.DiscoveryStrategy.Reiterating.INSTANCE)
                .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
                .with(AgentBuilder.TypeStrategy.Default.REBASE);

        AgentBuilder.Identified.Extendable a = agentBuilder
                .type(ElementMatchers.is(Thread.class))
                .transform(
                        new AgentBuilder.Transformer.ForAdvice(Advice.withCustomMapping().bootstrap(bootstrap))
                                .include(Main.class.getClassLoader())
                                .advice(ElementMatchers.isConstructor(), ThreadConstructorAdvice.class.getName()
                ));

/*        new AgentBuilder
                .Default()
                .ignore(none())
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .disableClassFormatChanges()
                .type(isSubTypeOf(java.util.concurrent.Executor.class))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.visit(Advice.to(ExecutorExecuteSubmitAdvice.class)
                                .on(named("execute").or(named("submit"))))
                ).installOn(inst);*/

        a = a
                .type(isSubTypeOf(java.util.concurrent.Executor.class))
                .transform(
                        new AgentBuilder.Transformer.ForAdvice(Advice.withCustomMapping().bootstrap(bootstrap))
                                .include(Main.class.getClassLoader())
                                .advice(ElementMatchers.isMethod().and(ElementMatchers.named("execute").or(named("submit"))), ExecutorExecuteSubmitAdvice.class.getName()
                ));

/*        new AgentBuilder
                .Default()
                .ignore(none())
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .disableClassFormatChanges()
                .type(isSubTypeOf(java.util.concurrent.Executor.class))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.visit(Advice.to(ExecutorConstructorAdvice.class)
                                .on(ElementMatchers.isConstructor()))).installOn(inst);*/

        a = a
                .type(isSubTypeOf(java.util.concurrent.Executor.class))
                .transform(
                        new AgentBuilder.Transformer.ForAdvice(Advice.withCustomMapping().bootstrap(bootstrap))
                                .include(Main.class.getClassLoader())
                                .advice(ElementMatchers.isConstructor(), ExecutorConstructorAdvice.class.getName()
                                ));

/*        new AgentBuilder
                .Default()
                .ignore(none())
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .disableClassFormatChanges()
                .type(isSubTypeOf(java.lang.Thread.class))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.visit(Advice.to(ThreadStartAdvice.class)
                                .on(named("start")))
                ).installOn(inst);*/

        a = a
                .type(isSubTypeOf(java.lang.Thread.class))
                .transform(
                        new AgentBuilder.Transformer.ForAdvice(Advice.withCustomMapping().bootstrap(bootstrap))
                                .include(Main.class.getClassLoader())
                                .advice(ElementMatchers.isMethod().and(ElementMatchers.named("start")), ThreadStartAdvice.class.getName()
                                ));

/*        new AgentBuilder
                .Default()
                .ignore(none())
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .disableClassFormatChanges()
                .type(any())
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.visit(Advice.to(ThreadStatusScanner.class)
                                .on(named("main")
                                        .and(takesArguments(String[].class))
                                        .and(isPublic())
                                        .and(isStatic()))))
                .installOn(inst);*/

  /*      new AgentBuilder
                .Default()
                .ignore(none())
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .disableClassFormatChanges()
                .type(isSubTypeOf(java.lang.Thread.class))
                .transform(
                        new AgentBuilder.Transformer.ForAdvice(Advice.withCustomMapping().bootstrap(bootstrap))
                                .include(Main.class.getClassLoader())
                                .advice(ElementMatchers.isMethod().and(ElementMatchers.named("start")), ThreadStartAdvice.class.getName()
                                )).installOn(inst);*/

/*        new AgentBuilder
                .Default()
                .ignore(none())
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .disableClassFormatChanges()
                .type(isSubTypeOf(Executor.class))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.visit(Advice.to(ExecutorShutdownAdvice.class)
                                .on(named("shutdown")))
                ).installOn(inst);*/

        a = a
                .type(isSubTypeOf(Executor.class))
                .transform(
                        new AgentBuilder.Transformer.ForAdvice(Advice.withCustomMapping().bootstrap(bootstrap))
                                .include(Main.class.getClassLoader())
                                .advice(ElementMatchers.isMethod().and(ElementMatchers.named("shutdown")), ExecutorShutdownAdvice.class.getName()
                                ));


/*        new AgentBuilder
                .Default()
                .ignore(ElementMatchers.none()) // Nie ignorujemy żadnych klas
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION) // Pozwalamy na retransforamację klas
                .disableClassFormatChanges()
                .type(ElementMatchers.isSubTypeOf(Lock.class)) // Przechwytujemy dowolną implementację Lock
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.visit(Advice.to(LockAdvice.class)
                                .on(ElementMatchers.named("lock")))
                )
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.visit(Advice.to(UnlockAdvice.class)
                                .on(ElementMatchers.named("unlock")))
                )
                .installOn(inst);*/

        a = a
                .type(isSubTypeOf(Lock.class))
                .transform(
                        new AgentBuilder.Transformer.ForAdvice(Advice.withCustomMapping().bootstrap(bootstrap))
                                .include(Main.class.getClassLoader())
                                .advice(ElementMatchers.isMethod().and(ElementMatchers.named("lock")), LockAdvice.class.getName()
                                ))
                .transform(
                        new AgentBuilder.Transformer.ForAdvice(Advice.withCustomMapping().bootstrap(bootstrap))
                                .include(Main.class.getClassLoader())
                                .advice(ElementMatchers.isMethod().and(ElementMatchers.named("unlock")), UnlockAdvice.class.getName()
                                ));

        // inst.appendToBootstrapClassLoaderSearch(new JarFile(new File("C:\\Users\\Piotr\\OneDrive\\Pulpit\\Studia\\Magisterka\\production\\lib\\thread-agent-1-0.jar")));
        a.installOn(inst);
        try {
            System.out.println("Attempting to retransform classes");
            for (Class<?> clazz : inst.getAllLoadedClasses()) {
                if (java.util.concurrent.Executor.class.isAssignableFrom(clazz)) {
                    inst.retransformClasses(clazz);
                }
            }
            inst.retransformClasses(Thread.class);
            System.out.println("Retransformation completed successfully.");
        } catch (UnmodifiableClassException e) {
            System.out.println("Error occurred during retransforming classes" + e.getMessage());
        }
        System.out.println("Transformation and Retransformation finished, running target application...\n\n\n");

    }

    public static CallSite bootstrap(MethodHandles.Lookup sourceMethodLookup, String adviceMethodName, MethodType adviceMethodType, String adviceClassName, Object[] args) {
        try {
            Class<?> adviceClass = Class.forName(adviceClassName);
            MethodHandle aStatic = MethodHandles.lookup().findStatic(adviceClass, adviceMethodName, adviceMethodType);
            return new ConstantCallSite(aStatic);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static byte[] loadBootstrapAgentBytes() throws IOException {
        byte[] classBytes;
        try (InputStream in = Main.class.getClassLoader().getResourceAsStream(BOOTSTRAP_AGENT_CLASS.replace('.', '/').concat(".class"))) {
            if (in != null) {
                classBytes = inputStreamToBytes(in);
            } else {
                System.out.println("Classloader resource not found org.threadmonitoring.bootstrap.BootstrapAgent");
                throw new IOException("Classloader resource not found org.threadmonitoring.bootstrap.BootstrapAgent");
            }
            return classBytes;
        }
    }

    private static byte[] inputStreamToBytes(InputStream in) throws IOException {
        try(ByteArrayOutputStream os = new ByteArrayOutputStream()){
            byte[] buffer = new byte[4 * 0x400];
            int length;
            while((length = in.read(buffer)) != -1){
                os.write(buffer, 0, length);
            }
            return os.toByteArray();
        }
    }
}
