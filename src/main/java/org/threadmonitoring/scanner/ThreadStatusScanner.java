package org.threadmonitoring.scanner;

import net.bytebuddy.asm.Advice;

import java.util.Arrays;

import static org.threadmonitoring.advices.ThreadConstructorAdvice.threads;
import static org.threadmonitoring.model.ExecutorModel.EXECUTOR_MAP;

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
            synchronized (EXECUTOR_MAP) {
                EXECUTOR_MAP.forEach(((executor, executorModel) -> {
                    System.out.println("Map with Executor: " + Arrays.toString(executorModel.getConstructorStackTrace()) + executorModel.isActive());
                }));
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
