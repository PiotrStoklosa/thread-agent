package org.threadmonitoring.advices;

import net.bytebuddy.asm.Advice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public final class ThreadConstructorAdvice {

    public static Logger LOGGER;
    public static AtomicInteger numberOfThreads;
    public static Map<Long, ExecutorService> threadToExecutorServiceMap;
    public static List<Thread> threads;

    public static void initialize() {
        LOGGER = LogManager.getLogger(ThreadConstructorAdvice.class);
        numberOfThreads = new AtomicInteger(0);
        threadToExecutorServiceMap = new WeakHashMap<>();
        threads = Collections.synchronizedList(new ArrayList<>());
    }

    @Advice.OnMethodExit(inline = false)
    public static void interceptExit(
            @Advice.This Thread thread
    ) {

        numberOfThreads.addAndGet(1);
        threads.add(thread);
        ExecutorService executorService = ExecutorServiceExecuteSubmitAdvice.currentExecutorService.get();
        if (executorService != null) {
            synchronized (threadToExecutorServiceMap) {
                threadToExecutorServiceMap.put(thread.getId(), executorService);
            }
            LOGGER.info("Thread {} created by ExecutorService: {}", thread.getName(), executorService);
        } else {
            LOGGER.info("Thread {} created independently", thread.getName());
        }
    }
}
