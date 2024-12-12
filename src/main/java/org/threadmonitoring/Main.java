package org.threadmonitoring;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.agent.builder.AgentBuilder;
import org.threadmonitoring.advices.*;

import static net.bytebuddy.matcher.ElementMatchers.none;

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

        try {
            System.out.println("Attempting to retransform class: java.lang.Thread");
            inst.retransformClasses(Thread.class);
            System.out.println("Retransformation completed successfully.");
        } catch (UnmodifiableClassException e) {
            System.out.println("Error occurred during retransforming classes" + e.getMessage());
        }
        System.out.println("Transformation and Retransformation finished, running target application...\n\n\n");

    }

}
