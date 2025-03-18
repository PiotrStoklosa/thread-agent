package org.threadmonitoring.advices;

import net.bytebuddy.asm.Advice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.threadmonitoring.model.ExecutorModel;

import java.lang.reflect.Executable;
import java.util.concurrent.Executor;

import static org.threadmonitoring.model.ExecutorModel.EXECUTOR_MAP;

public class ExecutorConstructorAdvice {

    public static Logger LOGGER = LogManager.getLogger(ExecutorConstructorAdvice.class);
    public static ThreadLocal<Boolean> activeConstructor = ThreadLocal.withInitial(() -> false);
    public static ThreadLocal<StackTraceElement[]> stackTrace = new ThreadLocal<>();
    public static ThreadLocal<Integer> stackOfExecutorConstructors = ThreadLocal.withInitial(() -> 0);

    @Advice.OnMethodEnter(inline = false)
    public static void interceptEntry(
            @Advice.Origin Executable methodOrConstructor,
            @Advice.AllArguments Object[] args
    ) {
        stackOfExecutorConstructors.set(stackOfExecutorConstructors.get() + 1);
        if (!activeConstructor.get()) {
            activeConstructor.set(true);
            stackTrace.set(Thread.currentThread().getStackTrace());
        }
    }

    @Advice.OnMethodExit(inline = false)
    public static void interceptExit(
            @Advice.Origin Executable methodOrConstructor,
            @Advice.AllArguments Object[] args,
            @Advice.This Executor executor
    ) {

        if (stackOfExecutorConstructors.get() == 1) {
            activeConstructor.set(false);
            StackTraceElement[] s = Thread.currentThread().getStackTrace();
            for (int i = 1; i < s.length; i++) {
                String className = s[i].getClassName();
                if (!className.startsWith("java.util.concurrent") &&
                        !className.equals("java.lang.Thread") &&
                        !className.startsWith("java.security") &&
                        !className.startsWith("org.threadmonitoring.advices")) {
                    LOGGER.info("Thread {} created new Executor at {}", Thread.currentThread().getName(), s[i]);
                    break;
                }
            }

            EXECUTOR_MAP.put(executor, new ExecutorModel(stackTrace.get()));
            activeConstructor.set(false);
        }
        stackOfExecutorConstructors.set(stackOfExecutorConstructors.get() - 1);
    }

}