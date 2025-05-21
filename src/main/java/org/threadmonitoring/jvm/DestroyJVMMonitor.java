package org.threadmonitoring.jvm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.threadmonitoring.advices.ThreadConstructorAdvice.threads;

public class DestroyJVMMonitor {

    public static Logger LOGGER = LogManager.getLogger(DestroyJVMMonitor.class);


    public static void displayAllThreads() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (threads) {
                for (Thread thread : threads) {
                    Thread.State state = thread.getState();
                    if (state != Thread.State.TERMINATED
                            && !"DestroyJavaVM".equals(thread.getName())
                            && !"Thread-1".equals(thread.getName())) {
                        LOGGER.warn("Thread {} was forced to terminate during JVM shutdown with state: {}",
                                thread.getName(), state);
                    }

                }
            }
        }));
    }

}
