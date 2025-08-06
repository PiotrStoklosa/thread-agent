package org.threadmonitoring.substitution;

import org.threadmonitoring.analyzer.DeadlockAnalyzer;
import org.threadmonitoring.logging.SynchronizedLogger;

public class WaitSubstitution {
    public static void wait2(Object o) throws InterruptedException {
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " called wait() on object: " + o.toString()
                , WaitSubstitution.class
                , "INFO");
        SynchronizedLogger.logExit(o);
        DeadlockAnalyzer.beforeWaitingForResource(Thread.currentThread(), o);
        o.wait();
        SynchronizedLogger.logEnter2(o);
        DeadlockAnalyzer.afterWaitingForResource(Thread.currentThread(), o, false);
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " got notified on object: " + o.toString()
                , WaitSubstitution.class
                , "INFO");
    }
}
