package org.threadmonitoring;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.threadmonitoring.advices.*;
// import org.threadmonitoring.connection.Publisher;
import org.threadmonitoring.scanner.ThreadStatusScanner;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.jar.JarFile;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class Main {
    public static final Logger logger = LogManager.getLogger(ThreadConstructorAdvice.class);;
    public static void run(Instrumentation inst) throws IOException, ClassNotFoundException {
        // Publisher.run();

        try {
            Class.forName("org.apache.logging.log4j.LogManager");
            System.out.println("Log4j2 successfully loaded.");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load Log4j2!");
            e.printStackTrace();
        }
        System.setProperty("log4j.configurationFile", "file:C:\\Users\\Piotr\\OneDrive\\Pulpit\\Studia\\Magisterka\\thread-agent\\src\\main\\resources\\log4j2.xml");
        Class<?> c = org.apache.logging.log4j.LogManager.class;

        ClassLoader cl = c.getClassLoader();

        // Wyświetlenie informacji o ClassLoaderze
        System.out.println("ClassLoader for class " + c.getName() + ": " + cl);

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
                        builder.visit(Advice.to(ExecutorExecuteSubmitAdvice.class)
                                .on(named("execute").or(named("submit"))))
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

        new AgentBuilder
                .Default()
                .ignore(none())
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .disableClassFormatChanges()
                .type(isSubTypeOf(java.lang.Thread.class))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.visit(Advice.to(ThreadStartAdvice.class)
                                .on(named("start")))
                ).installOn(inst);

        new AgentBuilder
                .Default()
                .ignore(none())
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .disableClassFormatChanges()
                .type(any())
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.visit(Advice.to(ThreadStatusScanner.class)
                                .on(named("main")
                                        .and(takesArguments(String[].class))
                                        .and(isPublic())
                                        .and(isStatic()))))
                .installOn(inst);



        new AgentBuilder
                .Default()
                .ignore(none())
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .disableClassFormatChanges()
                .type(isSubTypeOf(Executor.class))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.visit(Advice.to(ExecutorShutdownAdvice.class)
                                .on(named("shutdown")))
                ).installOn(inst);


        new AgentBuilder
                .Default()
                .ignore(ElementMatchers.none()) // Nie ignorujemy żadnych klas
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION) // Pozwalamy na retransforamację klas
                .disableClassFormatChanges()
                .type(ElementMatchers.isSubTypeOf(Lock.class)) // Przechwytujemy dowolną implementację Lock
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.visit(Advice.to(LockAdvice.class)
                                .on(ElementMatchers.named("lock")))
                )
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.visit(Advice.to(UnlockAdvice.class)
                                .on(ElementMatchers.named("unlock")))
                )
                .installOn(inst);

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
