package org.threadmonitoring;

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
        System.out.println("URI: " + uri);
        Path lib = Paths.get(uri).getParent().resolve("./lib");
        System.out.println("lib: " + lib);

        try (Stream<Path> pathStream = Files.list(lib)) {
            URL[] urls = pathStream.map(Path::toUri).map(uri1 -> {
                try {
                    return uri1.toURL();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }).toArray(URL[]::new);
            for (URL url : urls) {
                System.out.println(url);
            }
            AgentClassLoader agentClassLoader = new AgentClassLoader(urls, Thread.currentThread().getContextClassLoader());
            Class<?> agentMain = agentClassLoader.loadClass("org.threadmonitoring.Main");
            Method run = agentMain.getDeclaredMethod("run", Instrumentation.class);

            String jarPath1 = Paths.get(uri).getParent().resolve("C:\\Users\\Piotr\\OneDrive\\Pulpit\\Studia\\Magisterka\\production\\lib\\thread-agent-1-0.jar").toString();
            String jarPath2 = Paths.get(uri).getParent().resolve("C:\\Users\\Piotr\\OneDrive\\Pulpit\\Studia\\Magisterka\\production\\thread-agent.jar").toString();
            System.out.println(jarPath1);
            inst.appendToSystemClassLoaderSearch(new JarFile(jarPath1));
            System.out.println(jarPath2);
            inst.appendToSystemClassLoaderSearch(new JarFile(jarPath2));

            run.invoke(null, inst);
        }
    }
}