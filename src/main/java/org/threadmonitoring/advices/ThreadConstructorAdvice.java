package org.threadmonitoring.advices;


import net.bytebuddy.asm.Advice;

import java.lang.reflect.Executable;

public final class ThreadConstructorAdvice {

    public static int numberOfThreads = 0;

    @Advice.OnMethodEnter
    public static void interceptEntry(
            @Advice.Origin Executable methodOrConstructor,
            @Advice.AllArguments Object[] args
    ) {
        if (args != null) {
            if (args.length == 6){
                System.out.println("Creating thread named " + args[2]);
            }
        }
    }

    @Advice.OnMethodExit
    public static void interceptExit(
            @Advice.Origin Executable methodOrConstructor,
            @Advice.AllArguments Object[] args
    ) {
        if (args != null) {
            if (args.length == 6){
                numberOfThreads ++;
                System.out.println("Created successfully thread named " + args[2]);
                System.out.println("Already created " + numberOfThreads + " threads");
            }
        }
    }

}
