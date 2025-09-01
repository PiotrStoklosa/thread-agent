package org.threadmonitoring.substitution;

import static org.threadmonitoring.analyzer.DeadlockAnalyzer.isHoldingMonitor;
import static org.threadmonitoring.substitution.LoggingNotifier.emergencyLog;

public class NotifySubstitution {
    public static void notify2(Object o) {
        if (!isHoldingMonitor(o)) {
            emergencyLog("Thread " + Thread.currentThread().getName() + " called notify() on object: " + o.toString() +
                    " without holding the monitor on required object! Application will throw IllegalMonitorStateException");
        }
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " called notify() on object: " + o.toString()
                , NotifySubstitution.class
                , "INFO");
        o.notify();
    }
}
