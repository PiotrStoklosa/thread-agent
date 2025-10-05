package org.threadmonitoring.substitution.call;

import org.threadmonitoring.logger.SynchronizedLogger;

public class SynchronizedCall {

    public static void alertBeforeSynchronizedMethodEntry2(Object monitor) {
        SynchronizedLogger.logEnter(monitor);
    }

}
