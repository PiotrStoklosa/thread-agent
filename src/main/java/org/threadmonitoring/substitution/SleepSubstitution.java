package org.threadmonitoring.substitution;


public class SleepSubstitution {

    public static void sleep2(long millis) throws InterruptedException {
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " started sleeping for " + millis + " ms"
                , SleepSubstitution.class
                , "INFO");
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LoggingNotifier.log(
                    "Thread " + Thread.currentThread().getName() + " finished sleeping earlier because of " + e
                    , SleepSubstitution.class
                    , "INFO");
            throw e;
        }
        LoggingNotifier.log(
                "Thread " + Thread.currentThread().getName() + " finished sleeping for " + millis + " ms"
                , SleepSubstitution.class
                , "INFO");
    }
}
