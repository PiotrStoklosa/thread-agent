package org.threadmonitoring;

public class NotifySubstitution {
    public static void notify2(Object o) {
        LoggingNotifier.log("notify() has been called on object: " + o.toString());
        System.out.println("notify() has been called on object: " + o.toString());
        o.notify();
    }
}
