package org.threadmonitoring.logging;

import org.threadmonitoring.analyzer.DeadlockAnalyzer;
import org.threadmonitoring.substitution.LoggingNotifier;

import java.util.HashMap;
import java.util.Map;


public class SynchronizedLogger {

    private static final ThreadLocal<Object> heldMonitor = new ThreadLocal<>();
    private static final ThreadLocal<Map<Object, Integer>> synchronizedMap = ThreadLocal.withInitial(HashMap::new);

    public static void logEnter(Object monitor) {
        if (!synchronizedMap.get().containsKey(monitor) || synchronizedMap.get().get(monitor) == 0) {
            LoggingNotifier.log("Thread " + Thread.currentThread().getName() +
                    " waiting to hold monitor: " +
                    monitor.toString(), SynchronizedLogger.class, "INFO");
            DeadlockAnalyzer.beforeWaitingForResource(Thread.currentThread(), monitor);
        }
        heldMonitor.set(monitor);
    }

    public static void logEnter2() {
        if (heldMonitor.get() != null) {
            Object monitor = heldMonitor.get();
            heldMonitor.remove();
            if (!synchronizedMap.get().containsKey(monitor) || synchronizedMap.get().get(monitor) == 0) {
                LoggingNotifier.log("Thread " + Thread.currentThread().getName() + " holds monitor: " + monitor, SynchronizedLogger.class, "INFO");
                DeadlockAnalyzer.afterWaitingForResource(Thread.currentThread(), monitor, true);
            }
            if (!synchronizedMap.get().containsKey(monitor)) {
                synchronizedMap.get().put(monitor, 1);
            } else {
                synchronizedMap.get().put(monitor, synchronizedMap.get().get(monitor) + 1);
            }
        }
    }

    public static void logEnter2(Object monitor) {
        heldMonitor.set(monitor);
        logEnter2();
    }

    public static void logExit(Object monitor) {
        if (synchronizedMap.get().get(monitor) == 1) {
            LoggingNotifier.log("Thread " + Thread.currentThread().getName() + " released monitor: " + monitor.toString(), SynchronizedLogger.class, "INFO");
            DeadlockAnalyzer.afterReleasingResource(monitor);
        }
        synchronizedMap.get().put(monitor, synchronizedMap.get().get(monitor) - 1);
    }
}
