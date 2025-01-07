package org.threadmonitoring.advices;

import net.bytebuddy.asm.Advice;

public final class ThreadStartAdvice {

    @Advice.OnMethodEnter
    public static void interceptEntry(
            @Advice.This Thread thread
    ) {
        System.out.println("Started new Thread " + thread);
    }

}
