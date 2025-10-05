package org.threadmonitoring.substitution;

import org.threadmonitoring.analyzer.DeadlockAnalyzer;
import org.threadmonitoring.logger.SynchronizedLogger;

import static org.threadmonitoring.analyzer.DeadlockAnalyzer.isHoldingMonitor;
import static org.threadmonitoring.substitution.LoggingNotifier.emergencyLog;

public class WaitSubstitution {
    public static void wait2(Object o) throws InterruptedException {
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " called wait() on object: " + o.toString()
                , WaitSubstitution.class
                , "INFO");
        if (!isHoldingMonitor(o)) {
            emergencyLog("Thread " + Thread.currentThread().getName() + " called wait() on object: " + o.toString() +
                    " without holding the monitor on required object! Application will throw IllegalMonitorStateException");
        } else {
            SynchronizedLogger.logExit(o);
            DeadlockAnalyzer.beforeWaitingForResource(Thread.currentThread(), o);
        }
        o.wait();
        SynchronizedLogger.logEnter2(o);
        DeadlockAnalyzer.afterWaitingForResource(Thread.currentThread(), o, false);
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " got notified on object: " + o.toString()
                , WaitSubstitution.class
                , "INFO");
    }

    public static void waitUntil2(Object o, long timeoutMillis) throws InterruptedException {
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " called wait(" + timeoutMillis +
                        ") on object: " + o.toString()
                , WaitSubstitution.class
                , "INFO");
        if (!isHoldingMonitor(o)) {
            emergencyLog("Thread " + Thread.currentThread().getName() + " called wait(" + timeoutMillis
                    + ") on object: " + o.toString() +
                    " without holding the monitor on required object! Application will throw IllegalMonitorStateException");
        } else {
            SynchronizedLogger.logExit(o);
            DeadlockAnalyzer.beforeWaitingForResource(Thread.currentThread(), o);
        }
        o.wait(timeoutMillis);
        SynchronizedLogger.logEnter2(o);
        DeadlockAnalyzer.afterWaitingForResource(Thread.currentThread(), o, false);
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " got notified on object: " + o.toString()
                , WaitSubstitution.class
                , "INFO");
    }

}
