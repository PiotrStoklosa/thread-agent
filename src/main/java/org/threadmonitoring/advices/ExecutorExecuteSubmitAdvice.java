package org.threadmonitoring.advices;

import net.bytebuddy.asm.Advice;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import static org.threadmonitoring.model.ExecutorModel.EXECUTOR_MAP;

public class ExecutorExecuteSubmitAdvice {

    public static final ThreadLocal<Executor> currentExecutor = new ThreadLocal<>();
    public static Optional<String> frame;

    @Advice.OnMethodEnter
    public static void onEnter(@Advice.Origin String method, @Advice.This Executor executor) {



        System.out.println("Entering");
        currentExecutor.set(executor);

        frame = Optional.empty();
        AtomicReference<String> foundFrame = new AtomicReference<>(null);

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        for (StackTraceElement element : stackTrace) {
            if (!element.getClassName().contains("java.util.concurrent") && !element.getClassName().contains("java.lang.Thread")) {
                foundFrame.set(element.getClassName() + "." + element.getMethodName() + " (" + element.getFileName() + ":" + element.getLineNumber() + ")");
                break;
            }
        }

        frame = Optional.ofNullable(foundFrame.get());

        if (frame.isPresent()) {
            String f = frame.get();
            System.out.println("Place: " + f);
            if (method.contains("submit")) {
                EXECUTOR_MAP.get(executor).addSubmitPlace(f);
            } else {
                EXECUTOR_MAP.get(executor).addExecutePlace(f);
            }
        }
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit() {
        currentExecutor.remove();
    }
}