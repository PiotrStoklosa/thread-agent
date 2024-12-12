package org.threadmonitoring;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.agent.builder.AgentBuilder;

import static net.bytebuddy.matcher.ElementMatchers.none;

public class Main {

    public static void premain(String agentArgs, Instrumentation inst) {

        System.out.println("Test");
        new AgentBuilder.Default()
                .ignore(none())
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .disableClassFormatChanges()
                .type(ElementMatchers.is(Thread.class))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder
                                .constructor(ElementMatchers.any())
                                .intercept(MethodDelegation.to(ConstructorAdvice.class))
                                .method(ElementMatchers.named("start"))
                                .intercept(MethodDelegation.to(StartMethodAdvice.class))
                ).installOn(inst);

        try {
            System.out.println("Attempting to retransform class: java.lang.Thread");
            inst.retransformClasses(Thread.class);
            System.out.println("Retransformation completed successfully.");
        } catch (UnmodifiableClassException e) {
            e.printStackTrace();
        }
    }

    public static class ConstructorAdvice {

        static int numberOfThread = -1;

        @Advice.OnMethodEnter
        public static void onEnter() {
            numberOfThread ++;
            System.out.println("Created new Thread! There are " + numberOfThread + " threads!");
        }
    }
    public static class StartMethodAdvice {

        @Advice.OnMethodEnter
        public static void onEnter() {
            System.out.println("Thread is starting!");
        }
    }
}
