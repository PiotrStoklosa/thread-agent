package org.threadmonitoring.advices;

import net.bytebuddy.asm.Advice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import static org.threadmonitoring.model.ExecutorModel.EXECUTOR_MAP;

public class ExecutorExecuteSubmitAdvice {

    public static Logger LOGGER = LogManager.getLogger(ExecutorExecuteSubmitAdvice.class);
    public static final ThreadLocal<Executor> currentExecutor = new ThreadLocal<>();
    public static Optional<String> frame;

    @Advice.OnMethodEnter(inline = false)
    public static void onEnter(@Advice.Origin String method, @Advice.This Executor executor) {

        currentExecutor.set(executor);

        frame = Optional.empty();
        AtomicReference<String> foundFrame = new AtomicReference<>(null);

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        for (StackTraceElement element : stackTrace) {
            if (!element.getClassName().contains("java.util.concurrent") &&
                    !element.getClassName().contains("java.lang.Thread") &&
                    !element.getClassName().contains("org.threadmonitoring.advices")) {
                foundFrame.set(element.getClassName() + "." + element.getMethodName() + " (" + element.getFileName() + ":" + element.getLineNumber() + ")");
                break;
            }
        }

        frame = Optional.ofNullable(foundFrame.get());

        if (frame.isPresent()) {
            String f = frame.get();
            if (method.contains("submit")) {
                LOGGER.info("Task submitted by thread <{}> at {}", Thread.currentThread().getName(), f);
                EXECUTOR_MAP.get(executor).addSubmitPlace(f);
            } else {
                LOGGER.info("Task executed by thread <{}> at {}", Thread.currentThread().getName(), f);
                EXECUTOR_MAP.get(executor).addExecutePlace(f);
            }
        }
    }

    @Advice.OnMethodExit(inline = false, onThrowable = Throwable.class)
    public static void onExit() {
        currentExecutor.remove();
    }
}