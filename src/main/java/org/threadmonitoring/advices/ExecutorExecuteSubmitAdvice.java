package org.threadmonitoring.advices;

import net.bytebuddy.asm.Advice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.threadmonitoring.configuration.Configuration;

import java.util.Optional;
import java.util.concurrent.Executor;

import static org.threadmonitoring.model.ExecutorModel.EXECUTOR_MAP;

public class ExecutorExecuteSubmitAdvice {

    public static Logger LOGGER = LogManager.getLogger(ExecutorExecuteSubmitAdvice.class);
    public static final ThreadLocal<Executor> currentExecutor = new ThreadLocal<>();

    @Advice.OnMethodEnter(inline = false)
    public static void onEnter(@Advice.Origin String method, @Advice.This Executor executor) {
        if (currentExecutor.get() == null) {
            currentExecutor.set(executor);

            Optional<StackWalker.StackFrame> place = StackWalker.getInstance()
                    .walk(frames -> frames.filter(f -> Configuration.monitoredPackages.stream().anyMatch(f.getClassName()::startsWith))
                            .findFirst());

            if (place.isPresent()) {
                String p = String.valueOf(place.get());
                if (method.contains("submit")) {
                    LOGGER.info("Task submitted by thread <{}> at {} on {}", Thread.currentThread().getName(), p, executor);
                    EXECUTOR_MAP.get(executor).addSubmitPlace(p);
                } else {
                    LOGGER.info("Task executed by thread <{}> at {} on {}", Thread.currentThread().getName(), p, executor);
                    EXECUTOR_MAP.get(executor).addExecutePlace(p);
                }
            }
        }
    }

    @Advice.OnMethodExit(inline = false, onThrowable = Throwable.class)
    public static void onExit() {
        currentExecutor.remove();
    }
}