package org.threadmonitoring;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.agent.builder.AgentBuilder;
import org.threadmonitoring.advices.*;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class Main {

    public static void premain(String agentArgs, Instrumentation inst) {

        System.out.println("Starting Thread Agent...");

        new AgentBuilder
                .Default()
                .ignore(none())
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .disableClassFormatChanges()
                .type(ElementMatchers.is(Thread.class))
                .transform((builder, typeDescription, classLoader, module, protectionDomain)
                        -> builder
                        .visit(Advice.to(ThreadConstructorAdvice.class)
                                .on(ElementMatchers.isConstructor()))).installOn(inst);

        new AgentBuilder
                .Default()
                .ignore(none())
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .disableClassFormatChanges()
                .type(isSubTypeOf(java.util.concurrent.Executor.class))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.visit(Advice.to(ExecutorAdvice.class)
                                .on(named("execute")))
                ).installOn(inst);

        new AgentBuilder
                .Default()
                .ignore(none())
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .disableClassFormatChanges()
                .type(isSubTypeOf(java.util.concurrent.Executor.class))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.visit(Advice.to(ExecutorConstructorAdvice.class)
                                .on(ElementMatchers.isConstructor()))).installOn(inst);

        try {
            System.out.println("Attempting to retransform classes");
            for (Class<?> clazz : inst.getAllLoadedClasses()) {
                if (java.util.concurrent.Executor.class.isAssignableFrom(clazz)) {
                    inst.retransformClasses(clazz);
                }
            }
            inst.retransformClasses(Thread.class);
            System.out.println("Retransformation completed successfully.");
        } catch (UnmodifiableClassException e) {
            System.out.println("Error occurred during retransforming classes" + e.getMessage());
        }
        System.out.println("Transformation and Retransformation finished, running target application...\n\n\n");

    }

}
