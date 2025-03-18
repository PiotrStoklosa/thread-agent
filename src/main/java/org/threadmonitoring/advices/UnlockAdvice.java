package org.threadmonitoring.advices;

import net.bytebuddy.asm.Advice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.locks.Lock;

public class UnlockAdvice {

    public static Logger LOGGER;

    public static void initialize() {
        LOGGER = LogManager.getLogger(UnlockAdvice.class);
    }

    @Advice.OnMethodEnter(inline = false)
    public static void interceptEntry(
            @Advice.This Lock lock
    ) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (stackTraceElement.getClassName().contains("java.util.concurrent.ThreadPoolExecutor") ||
                    stackTraceElement.getClassName().contains("org.apache.logging.log4j")) {
                return;
            }
        }
        LOGGER.info("Lock {} released by thread {} at {}",
                lock.toString(), Thread.currentThread().getName(), stackTrace[stackTrace.length - 1]);

    }

}