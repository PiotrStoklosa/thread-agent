package org.threadmonitoring.substitution;

public class WaitSubstitution {
    public static void wait2(Object o) throws InterruptedException {
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " called wait() on object: " + o.toString()
                , WaitSubstitution.class
                , "INFO");
        o.wait();
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " got notified on object: " + o.toString()
                , WaitSubstitution.class
                , "INFO");
    }
}
