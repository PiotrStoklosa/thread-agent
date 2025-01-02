package org.threadmonitoring;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import org.threadmonitoring.advices.ExecutorAdvice;
import org.threadmonitoring.advices.ExecutorConstructorAdvice;
import org.threadmonitoring.advices.ThreadConstructorAdvice;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.jar.JarFile;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class Main {

    public static void run(Instrumentation inst) throws IOException, ClassNotFoundException {

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

        inst.appendToBootstrapClassLoaderSearch(new JarFile(new File("C:\\Users\\Piotr\\OneDrive\\Pulpit\\Studia\\Magisterka\\production\\lib\\thread-agent-1-0.jar")));

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
