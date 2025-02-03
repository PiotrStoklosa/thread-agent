package org.threadmonitoring.advices;

import net.bytebuddy.asm.Advice;

import java.util.concurrent.Executor;

import static org.threadmonitoring.model.ExecutorModel.EXECUTOR_MAP;

public class ExecutorShutdownAdvice {

    @Advice.OnMethodEnter
    public static void interceptEntry(
            @Advice.This Executor executor
    ) {
        System.out.println("DEACTIVATED!");
        EXECUTOR_MAP.get(executor).deactivate();
    }

}