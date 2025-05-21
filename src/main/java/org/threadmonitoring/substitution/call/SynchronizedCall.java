package org.threadmonitoring.substitution.call;

import org.threadmonitoring.substitution.LoggingNotifier;

public class SynchronizedCall {

    public static void alertBeforeSynchronizedEntry2(Object o) {
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " tries to entry the synchronized block and " +
                        "acquire the monitor of object " + o.toString()
                , SynchronizedCall.class
                , "INFO");
    }

    public static void alertSynchronizedEntry2(Object o) {
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " entries the synchronized block and " +
                        "acquires the monitor of object " + o.toString()
                , SynchronizedCall.class
                , "INFO");
    }

    public static void alertSynchronizedExit2(Object o) {
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " exits the synchronized block and " +
                        "release the monitor of object " + o.toString()
                , SynchronizedCall.class
                , "INFO");
    }
}
