package org.threadmonitoring.scanner;

import net.bytebuddy.asm.Advice;

import static org.threadmonitoring.advices.ThreadConstructorAdvice.threads;

public class ThreadStatusScanner implements Runnable {

    public static volatile boolean firstScan = true;

    @Advice.OnMethodEnter
    public static void checkThreadStates() {
        if (firstScan) {
            synchronized (ThreadStatusScanner.class) {
                if (firstScan) {
                    Thread threadStatusScanner = new Thread(new ThreadStatusScanner());
                    threadStatusScanner.setDaemon(true);
                    threadStatusScanner.start();
                    firstScan = false;
                }
            }
        }
    }

    @Override
    public void run() {
        System.out.println("Thread state scanner started");
        while (true) {
            System.out.println("Scanning...");
            synchronized (threads) {
                for (Thread thread : threads) {
                    Thread.State state = thread.getState();
                    System.out.println("Thread " + thread.getName() + " has state: " + state);
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("Interruption");
                Thread.currentThread().interrupt();
                break;
            }

        }
    }
}
