package org.threadmonitoring.advices;

import net.bytebuddy.asm.Advice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.threadmonitoring.configuration.Configuration;
import org.threadmonitoring.model.ExecutorServiceModel;

import java.lang.reflect.Executable;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static org.threadmonitoring.model.ExecutorServiceModel.EXECUTOR_SERVICE_MAP;

public class ExecutorServiceConstructorAdvice {

    public static Logger LOGGER = LogManager.getLogger(ExecutorServiceConstructorAdvice.class);
    public static ThreadLocal<Integer> stackOfExecutorServiceConstructors = ThreadLocal.withInitial(() -> 0);


    @Advice.OnMethodEnter(inline = false)
    public static synchronized void interceptEntry(
            @Advice.Origin Executable methodOrConstructor,
            @Advice.AllArguments Object[] args
    ) {
        stackOfExecutorServiceConstructors.set(stackOfExecutorServiceConstructors.get() + 1);
    }

    @Advice.OnMethodExit(inline = false)
    public static synchronized void interceptExit(
            @Advice.Origin Executable methodOrConstructor,
            @Advice.AllArguments Object[] args,
            @Advice.This ExecutorService executorService
    ) {
        if (stackOfExecutorServiceConstructors.get() == 1) {

            Optional<StackWalker.StackFrame> place = StackWalker.getInstance()
                    .walk(frames -> frames.filter(f -> Configuration.monitoredPackages.stream().anyMatch(f.getClassName()::startsWith))
                            .findFirst());

            if (place.isPresent()) {
                String p = String.valueOf(place.get());
                LOGGER.info("Thread {} created new {} at {}", Thread.currentThread().getName(), executorService, p);
                EXECUTOR_SERVICE_MAP.put(executorService, new ExecutorServiceModel(p));
            }
        }
        stackOfExecutorServiceConstructors.set(stackOfExecutorServiceConstructors.get() - 1);
    }

}