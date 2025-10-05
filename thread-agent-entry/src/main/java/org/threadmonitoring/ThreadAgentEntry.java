package org.threadmonitoring;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class ThreadAgentEntry {
    public static void premain(String agentArgs, Instrumentation inst) throws URISyntaxException, IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {


        URI uri = AgentClassLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI();

        String jarDir = new File(uri).getParent();

        if (System.getProperty("log4j2.logdir") == null) {
            System.setProperty("log4j2.logdir"
                    , jarDir + "/logs");
        }

        Path api = Paths.get(uri).getParent().resolve("./api/agent-api-1.0.jar");
        JarFile jar = new JarFile(String.valueOf(api));
        inst.appendToBootstrapClassLoaderSearch(jar);

        Path lib = Paths.get(uri).getParent().resolve("./lib");

        try (Stream<Path> pathStream = Files.list(lib)) {
            URL[] urls = pathStream.map(Path::toUri).map(uri1 -> {
                try {
                    return uri1.toURL();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }).toArray(URL[]::new);

            AgentClassLoader agentClassLoader = new AgentClassLoader(urls, Thread.currentThread().getContextClassLoader());
            ClassLoader currentContextClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(agentClassLoader);
            Class<?> agentMain = agentClassLoader.loadClass("org.threadmonitoring.ThreadAgent");
            Method run = agentMain.getDeclaredMethod("run", Instrumentation.class, ClassLoader.class);

            run.invoke(null, inst, currentContextClassLoader);
        }
    }
}