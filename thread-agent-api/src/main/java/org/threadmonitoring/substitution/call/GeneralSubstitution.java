package org.threadmonitoring.substitution.call;

import org.threadmonitoring.substitution.LoggingNotifier;

public class GeneralSubstitution {
    public static void substitute2(String method) {
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " called " + method
                , GeneralSubstitution.class
                , "INFO");
    }
}
