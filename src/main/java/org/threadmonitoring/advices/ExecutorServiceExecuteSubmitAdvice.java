package org.threadmonitoring.advices;

import net.bytebuddy.asm.Advice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.threadmonitoring.configuration.Configuration;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static org.threadmonitoring.model.ExecutorServiceModel.EXECUTOR_SERVICE_MAP;

public class ExecutorServiceExecuteSubmitAdvice {

    public static Logger LOGGER = LogManager.getLogger(ExecutorServiceExecuteSubmitAdvice.class);
    public static final ThreadLocal<ExecutorService> currentExecutorService = new ThreadLocal<>();

    @Advice.OnMethodEnter(inline = false)
    public static void onEnter(@Advice.Origin String method, @Advice.This ExecutorService executorService) {
        if (currentExecutorService.get() == null) {
            currentExecutorService.set(executorService);

            Optional<StackWalker.StackFrame> place = StackWalker.getInstance()
                    .walk(frames -> frames.filter(f -> Configuration.monitoredPackages.stream().anyMatch(f.getClassName()::startsWith))
                            .findFirst());

            if (place.isPresent()) {
                String p = String.valueOf(place.get());
                if (method.contains("submit")) {
                    LOGGER.info("Task submitted by thread <{}> at {} on {}", Thread.currentThread().getName(), p, executorService);
                    EXECUTOR_SERVICE_MAP.get(executorService).addSubmitPlace(p);
                } else {
                    LOGGER.info("Task executed by thread <{}> at {} on {}", Thread.currentThread().getName(), p, executorService);
                    EXECUTOR_SERVICE_MAP.get(executorService).addExecutePlace(p);
                }
            }
        }
    }

    @Advice.OnMethodExit(inline = false, onThrowable = Throwable.class)
    public static void onExit() {
        currentExecutorService.remove();
    }
}