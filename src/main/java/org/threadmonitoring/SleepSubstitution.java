package org.threadmonitoring;


public class SleepSubstitution {

    public static void sleep2(long millis) throws InterruptedException {
        LoggingNotifier.log("Starting sleeping for " + millis + " ms");
        System.out.println("Starting sleeping for " + millis + " ms");
        Thread.sleep(millis);
        LoggingNotifier.log("Finished sleeping for " + millis + " ms");
        System.out.println("Finished sleeping for " + millis + " ms");
    }
}
