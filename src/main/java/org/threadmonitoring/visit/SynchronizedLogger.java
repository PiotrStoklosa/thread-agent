package org.threadmonitoring.visit;

import org.threadmonitoring.substitution.LoggingNotifier;


public class SynchronizedLogger {
    public static void logEnter(String threadName, String monitor) {
        LoggingNotifier.log("Thread " + threadName + " holds monitor: " + monitor, SynchronizedLogger.class, "INFO");
    }

    public static void logExit(String threadName, String monitor) {
        LoggingNotifier.log("Thread " + threadName + " released monitor: " + monitor, SynchronizedLogger.class, "INFO");
    }
}
