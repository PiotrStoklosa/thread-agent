package org.threadmonitoring.substitution;

public class NotifyAllSubstitution {
    public static void notifyAll2(Object o) {
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " called notifyAll() on object: " + o.toString()
                , NotifyAllSubstitution.class
                , "INFO");
        o.notifyAll();
    }
}
