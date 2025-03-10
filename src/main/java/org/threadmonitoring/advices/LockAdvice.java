package org.threadmonitoring.advices;

import net.bytebuddy.asm.Advice;

import java.util.Arrays;
import java.util.concurrent.locks.Lock;


public class LockAdvice {

    @Advice.OnMethodEnter
    public static void interceptEntry(
            @Advice.This Lock lock
    ) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        for (StackTraceElement stackTraceElement : stackTrace) {
            if (stackTraceElement.getClassName().contains("java.util.concurrent.ThreadPoolExecutor")) {
                return;
            }
        }
        StackTraceElement element = stackTrace[stackTrace.length - 1];
        System.out.println("[AGENT] Lock" + lock.toString() + " aquaired in class: " + element.getClassName() +
                " method: " + element.getMethodName() + " at line: " + element.getLineNumber());

    }
}