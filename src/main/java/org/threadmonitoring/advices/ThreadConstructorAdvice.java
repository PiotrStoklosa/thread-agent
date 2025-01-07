package org.threadmonitoring.advices;


import net.bytebuddy.asm.Advice;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.List;

import java.util.concurrent.Executor;

public final class ThreadConstructorAdvice {

    public static int numberOfThreads = 0;
    public static final Map<Long, Executor> threadToExecutorMap = new WeakHashMap<>();
    public static final List<Thread> threads = new ArrayList<>();

    @Advice.OnMethodEnter
    public static void interceptEntry(
            @Advice.Origin Executable methodOrConstructor,
            @Advice.AllArguments Object[] args
    ) {
        if (args != null) {
            if (args.length == 6) {
                System.out.println("Creating thread named " + args[2]);
            }
        }
    }

    @Advice.OnMethodExit
    public static void interceptExit(
            @Advice.Origin Executable methodOrConstructor,
            @Advice.AllArguments Object[] args,
            @Advice.This Thread thread
    ) {
        if (args != null) {
            if (args.length == 6) {
                numberOfThreads++;
                System.out.println("Created successfully thread named " + args[2]);
                System.out.println("Already created " + numberOfThreads + " threads");

                synchronized (threads) {
                    threads.add(thread);
                }

                Executor executor = ExecutorExecuteSubmitAdvice.currentExecutor.get();
                if (executor != null) {
                    synchronized (threadToExecutorMap) {
                        threadToExecutorMap.put(thread.getId(), executor);
                    }
                    System.out.println("Thread " + thread.getId() + " created by Executor: " + executor);
                } else {
                    System.out.println("Thread created without Executor: " + thread.getId());
                }

            }
        }

    }


}
