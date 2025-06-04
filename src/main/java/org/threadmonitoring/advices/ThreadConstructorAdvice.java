package org.threadmonitoring.advices;

import net.bytebuddy.asm.Advice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public final class ThreadConstructorAdvice {

    public static Logger LOGGER;
    public static AtomicInteger numberOfThreads;
    public static Map<Long, Executor> threadToExecutorMap;
    public static List<Thread> threads;

    public static void initialize() {
        LOGGER = LogManager.getLogger(ThreadConstructorAdvice.class);
        numberOfThreads = new AtomicInteger(0);
        threadToExecutorMap = new WeakHashMap<>();
        threads = Collections.synchronizedList(new ArrayList<>());
    }

    @Advice.OnMethodExit(inline = false)
    public static void interceptExit(
            @Advice.Origin Executable methodOrConstructor,
            @Advice.AllArguments Object[] args,
            @Advice.This Thread thread
    ) {

        numberOfThreads.addAndGet(1);
        threads.add(thread);
        Executor executor = ExecutorExecuteSubmitAdvice.currentExecutor.get();
        if (executor != null) {
            synchronized (threadToExecutorMap) {
                threadToExecutorMap.put(thread.getId(), executor);
            }
            LOGGER.info("Thread {} created by Executor: {}", thread.getName(), executor);
        } else {
            LOGGER.info("Thread {} created independently", thread.getName());
        }
    }
}
