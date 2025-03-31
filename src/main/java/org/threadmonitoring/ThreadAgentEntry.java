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

        File jarFile = new File("C:\\Users\\Piotr\\OneDrive\\Pulpit\\Studia\\Magisterka\\thread-agent-api\\build\\libs\\Agent-API-1.0-SNAPSHOT.jar");
        JarFile jar = new JarFile(jarFile);
        inst.appendToBootstrapClassLoaderSearch(jar);

        URI uri = AgentClassLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI();
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
            Thread.currentThread().setContextClassLoader(agentClassLoader);
            Class<?> agentMain = agentClassLoader.loadClass("org.threadmonitoring.ThreadAgent");
            Method run = agentMain.getDeclaredMethod("run", Instrumentation.class);

            run.invoke(null, inst);
        }
    }
}