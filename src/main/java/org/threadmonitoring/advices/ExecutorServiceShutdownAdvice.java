package org.threadmonitoring.advices;

import net.bytebuddy.asm.Advice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;

import static org.threadmonitoring.model.ExecutorServiceModel.EXECUTOR_SERVICE_MAP;

public class ExecutorServiceShutdownAdvice {

    public static Logger LOGGER;

    public static void initialize() {
        LOGGER = LogManager.getLogger(ExecutorServiceShutdownAdvice.class);
    }

    @Advice.OnMethodEnter(inline = false)
    public static void interceptEntry(
            @Advice.This ExecutorService executorService
    ) {
        synchronized (ExecutorServiceShutdownAdvice.class) {
            if (EXECUTOR_SERVICE_MAP.containsKey(executorService) && EXECUTOR_SERVICE_MAP.get(executorService).isActive()) {
                EXECUTOR_SERVICE_MAP.get(executorService).deactivate();
                LOGGER.info("ExecutorService {} shutdown", executorService);
            }
        }
    }

}