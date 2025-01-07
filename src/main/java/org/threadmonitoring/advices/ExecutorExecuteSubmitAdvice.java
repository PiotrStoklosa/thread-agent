package org.threadmonitoring.advices;

import net.bytebuddy.asm.Advice;

import java.util.concurrent.Executor;

public class ExecutorExecuteSubmitAdvice {

    public static final ThreadLocal<Executor> currentExecutor = new ThreadLocal<>();

    @Advice.OnMethodEnter
    public static void onEnter(@Advice.This Executor executor) {

        currentExecutor.set(executor);

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        System.out.println("Executor: " + executor);
        System.out.println("New Task scheduled, executor:" + executor + ", stacktrace of thread creation: ");

        for (int i = 1; i < stackTrace.length; i++) {
            System.out.println(stackTrace[i]);
        }
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit() {
        currentExecutor.remove();
    }
}