package org.threadmonitoring.substitution.call;

import org.threadmonitoring.substitution.LoggingNotifier;

public class SynchronizedCall {
    public static void alertSynchronizedEntry2() {
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " entries the synchronized block"
                , SynchronizedCall.class
                , "INFO");
    }

    public static void alertSynchronizedExit2() {
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " exits the synchronized block"
                , SynchronizedCall.class
                , "INFO");
    }
}
