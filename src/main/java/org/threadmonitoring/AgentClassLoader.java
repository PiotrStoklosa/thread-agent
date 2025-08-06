package org.threadmonitoring;

import java.net.URL;
import java.net.URLClassLoader;

public class AgentClassLoader extends URLClassLoader {

    public AgentClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            /* These classes should not be loaded with the Agent Class Loader,
               as they need to be accessible in both the monitored application and the Thread Agent. */
            if (name.startsWith("org.threadmonitoring.substitution") ||
                    name.startsWith("org.threadmonitoring.logging") ||
                    name.startsWith("org.threadmonitoring.analyzer")){
                return getParent().loadClass(name);
            }
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                try {
                    c = findClass(name);
                } catch (ClassNotFoundException e) {
                    c = super.loadClass(name, false);
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }
}
