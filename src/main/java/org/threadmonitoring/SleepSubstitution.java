package org.threadmonitoring;


public class SleepSubstitution {

    public static void sleep2(long millis) throws InterruptedException {
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " started sleeping for " + millis + " ms"
                , SleepSubstitution.class
                , "INFO");
        Thread.sleep(millis);
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " finished sleeping for " + millis + " ms"
                , SleepSubstitution.class
                , "INFO");
    }
}
