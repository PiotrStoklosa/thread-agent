package org.threadmonitoring.analyzer;

import org.threadmonitoring.substitution.LoggingNotifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DeadlockAnalyzer {

    private static final Map<Object, Thread> lockedBy = new ConcurrentHashMap<>();

    private static final Map<Thread, Object> waitingFor = new ConcurrentHashMap<>();

    private static final List<String> destroyJavaVM = List.of("DestroyJavaVM");

    public static synchronized boolean isHoldingMonitor(Object monitor) {
        return destroyJavaVM.contains(Thread.currentThread().getName())
                || lockedBy.containsKey(monitor)
                && lockedBy.get(monitor).equals(Thread.currentThread());
    }

    public static synchronized void beforeWaitingForResource(Thread thread, Object resource) {
        waitingFor.put(thread, resource);
        Thread owner = lockedBy.get(resource);
        if (owner != null && owner != thread) {
            detectCycle(thread);
        }
    }

    public static synchronized void afterWaitingForResource(Thread thread, Object resource, boolean acquiredLock) {
        waitingFor.remove(thread);
        if (acquiredLock) {
            lockedBy.put(resource, thread);
        }
    }

    public static synchronized void afterReleasingResource(Object resource) {
        lockedBy.remove(resource);
    }

    private static synchronized void detectCycle(Thread start) {
        List<Thread> potentialThreads = new ArrayList<>();
        potentialThreads.add(start);
        Set<Thread> visited = new HashSet<>();
        Thread current = start;

        while (true) {
            Object wanted = waitingFor.get(current);
            if (wanted == null) {
                return;
            }
            Thread owner = lockedBy.get(wanted);
            if (owner == null) {
                return;
            }
            potentialThreads.add(owner);
            if (owner == start) {
                LoggingNotifier.emergencyLog("Potential deadlock detected!");
                LoggingNotifier.log("Potential deadlock detected! Please check emergency log for details",
                        DeadlockAnalyzer.class, "ERROR");
                LoggingNotifier.emergencyLog("Deadlock cycle: ");
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (Thread t : potentialThreads) {
                    if (first) {
                        first = false;
                        sb.append(t);
                    } else {
                        sb.append(" -> ").append(t);
                    }
                }
                LoggingNotifier.emergencyLog(sb.toString());
                LoggingNotifier.emergencyLog("Stack traces of involved threads:");
                potentialThreads.remove(potentialThreads.size() - 1);
                for (Thread t : potentialThreads) {
                    LoggingNotifier.emergencyLog("Thread: " + t.getName() + " (ID: " + t.getId() + ")");

                    try {
                        StackTraceElement[] stack = t.getStackTrace();
                        for (StackTraceElement frame : stack) {
                            if (!frame.getClassName().startsWith("org.threadmonitoring")
                                    && !frame.getClassName().startsWith("java.lang")
                                    && !frame.getClassName().startsWith("java.util")
                                    && !frame.getClassName().startsWith("java.base")) {
                                LoggingNotifier.emergencyLog("    at " + frame);
                            }
                        }
                    } catch (Exception e) {
                        LoggingNotifier.emergencyLog("    <Unable to get stack trace: " + e.getMessage() + ">");
                    }
                    LoggingNotifier.emergencyLog("");
                }
                return;
            }
            if (!visited.add(owner)) {
                return;
            }
            current = owner;
        }
    }

}
