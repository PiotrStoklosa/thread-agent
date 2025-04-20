package org.threadmonitoring.advices;

import net.bytebuddy.asm.Advice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.threadmonitoring.configuration.Configuration;
import org.threadmonitoring.model.ExecutorModel;

import java.lang.reflect.Executable;
import java.util.Optional;
import java.util.concurrent.Executor;

import static org.threadmonitoring.model.ExecutorModel.EXECUTOR_MAP;

public class ExecutorConstructorAdvice {

    public static Logger LOGGER = LogManager.getLogger(ExecutorConstructorAdvice.class);
    public static ThreadLocal<Integer> stackOfExecutorConstructors = ThreadLocal.withInitial(() -> 0);


    @Advice.OnMethodEnter(inline = false)
    public static synchronized void interceptEntry(
            @Advice.Origin Executable methodOrConstructor,
            @Advice.AllArguments Object[] args
    ) {
        stackOfExecutorConstructors.set(stackOfExecutorConstructors.get() + 1);
    }

    @Advice.OnMethodExit(inline = false)
    public static synchronized void interceptExit(
            @Advice.Origin Executable methodOrConstructor,
            @Advice.AllArguments Object[] args,
            @Advice.This Executor executor
    ) {
        if (stackOfExecutorConstructors.get() == 1) {

            Optional<StackWalker.StackFrame> place = StackWalker.getInstance()
                    .walk(frames -> frames.filter(f -> Configuration.monitoredPackages.stream().anyMatch(f.getClassName()::startsWith))
                            .findFirst());

            if (place.isPresent()) {
                String p = String.valueOf(place.get());
                LOGGER.info("Thread {} created new {} at {}", Thread.currentThread().getName(), executor, p);
                EXECUTOR_MAP.put(executor, new ExecutorModel(p));
            }
        }
        stackOfExecutorConstructors.set(stackOfExecutorConstructors.get() - 1);
    }

}