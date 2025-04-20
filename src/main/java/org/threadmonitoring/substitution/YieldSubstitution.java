package org.threadmonitoring.substitution;

public class YieldSubstitution {

    public static void yield2() throws InterruptedException {
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " called yield()",
                org.threadmonitoring.substitution.YieldSubstitution.class,
                "INFO");
        Thread.yield();
    }
}
