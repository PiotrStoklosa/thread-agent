package org.threadmonitoring.advices;

import net.bytebuddy.asm.Advice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.threadmonitoring.logging.EmergencyLogger;

public class InterruptAdvice {

    public static Logger LOGGER;

    public static void initialize() {
        LOGGER = LogManager.getLogger(InterruptAdvice.class);
    }

    @Advice.OnMethodEnter(inline = false)
    public static void interceptInterruptEntry(
            @Advice.This Thread thread
    ) {
        if (thread.getState().equals(Thread.State.TIMED_WAITING)) {
            EmergencyLogger.log("Thread " + Thread.currentThread() + " is interrupting thread " + thread +
                    " in TIMED_WAITING state! There is a possibility to throw InterruptedException!");
        } else {
            LOGGER.info("Thread {} tried to interrupt thread {}, but the state of that thread is {}",
                    Thread.currentThread().getName(), thread, thread.getState().toString());
        }
    }
}
