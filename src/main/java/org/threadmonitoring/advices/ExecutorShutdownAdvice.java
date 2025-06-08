package org.threadmonitoring.advices;

import net.bytebuddy.asm.Advice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executor;

import static org.threadmonitoring.model.ExecutorModel.EXECUTOR_MAP;

public class ExecutorShutdownAdvice {

    public static Logger LOGGER;

    public static void initialize() {
        LOGGER = LogManager.getLogger(ExecutorShutdownAdvice.class);
    }

    @Advice.OnMethodEnter(inline = false)
    public static void interceptEntry(
            @Advice.This Executor executor
    ) {
        synchronized (ExecutorShutdownAdvice.class) {
            if (EXECUTOR_MAP.containsKey(executor) && EXECUTOR_MAP.get(executor).isActive()) {
                EXECUTOR_MAP.get(executor).deactivate();
                LOGGER.info("Executor {} shutdown", executor);
            }
        }
    }

}