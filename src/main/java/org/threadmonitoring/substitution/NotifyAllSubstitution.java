package org.threadmonitoring.substitution;

import static org.threadmonitoring.analyzer.DeadlockAnalyzer.isHoldingMonitor;
import static org.threadmonitoring.substitution.LoggingNotifier.emergencyLog;

public class NotifyAllSubstitution {
    public static void notifyAll2(Object o) {
        if (!isHoldingMonitor(o)) {
            emergencyLog("Thread " + Thread.currentThread().getName() + " called notifyAll() on object: " + o.toString() +
                    " without holding the monitor on required object! Application will throw IllegalMonitorStateException");
        }
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " called notifyAll() on object: " + o.toString()
                , NotifyAllSubstitution.class
                , "INFO");
        o.notifyAll();
    }
}
