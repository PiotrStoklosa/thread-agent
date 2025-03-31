package org.threadmonitoring;

public class NotifySubstitution {
    public static void notify2(Object o) {
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " called notify() on object: " + o.toString()
                , NotifySubstitution.class
                , "INFO");
        o.notify();
    }
}
