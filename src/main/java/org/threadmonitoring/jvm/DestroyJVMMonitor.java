package org.threadmonitoring.jvm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.threadmonitoring.logging.EmergencyLogger;

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
                            && !"Thread-1".equals(thread.getName())
                            && !"Notification Thread".equals(thread.getName())
                    ) {

                        StackTraceElement[] stackTrace = thread.getStackTrace();
                        StringBuilder sb = new StringBuilder();
                        for (StackTraceElement element : stackTrace) {
                            sb.append("\tat ").append(element).append("\n");
                        }
                        String message = "Thread " + thread.getName() +
                                " was forced to terminate during JVM shutdown with state: " + state +
                                "\nStacktrace:\n" + sb;
                        EmergencyLogger.log(message);
                    }
                }
            }

        }));
    }

}
