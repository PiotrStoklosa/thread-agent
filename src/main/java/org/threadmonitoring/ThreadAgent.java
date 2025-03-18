package org.threadmonitoring;

import net.bytebuddy.agent.builder.AgentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.threadmonitoring.advices.*;
import org.threadmonitoring.model.AdviceRule;
import org.threadmonitoring.model.ExecutorModel;
import org.threadmonitoring.util.AdviceHandler;
import org.threadmonitoring.util.ClassLoadingHandler;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Method;
import java.util.List;

public class ThreadAgent {

    private static final Logger LOGGER = LogManager.getLogger(ThreadAgent.class);

    private static void printAndLog(String message) {
        LOGGER.info(message);
        System.out.println(message);
    }

    private static void printAndLogError(String message) {
        LOGGER.error(message);
        System.err.println(message);
    }

    private static void initializeClasses() {
        ThreadConstructorAdvice.initialize();
        ExecutorModel.initialize();
        LockAdvice.initialize();
        UnlockAdvice.initialize();
        ThreadStartAdvice.initialize();
        ExecutorShutdownAdvice.initialize();
    }

    public static void run(Instrumentation inst) {

        printAndLog("Initializing ThreadAgent before the target application to enable thread and executor monitoring");

        System.setProperty("log4j.configurationFile", "C:\\Users\\Piotr\\OneDrive\\Pulpit\\Studia\\Magisterka\\thread-agent\\src\\main\\resources\\log4j2.xml");
        printAndLog("Initialized log4j successfully");

        initializeClasses();
        printAndLog("Initialized advices");

        Method bootstrap = ClassLoadingHandler.handleClassLoading();
        List<AdviceRule> rules = AdviceHandler.createAdvices();
        AgentBuilder agent = AdviceHandler.buildAgentWithAdvices(rules, bootstrap);
        agent.installOn(inst);
        printAndLog("Advices created and installed");

        try {
            printAndLog("Attempting to retransform classes");
            for (Class<?> clazz : inst.getAllLoadedClasses()) {
                if (java.util.concurrent.Executor.class.isAssignableFrom(clazz)) {
                    inst.retransformClasses(clazz);
                }
            }
            inst.retransformClasses(Thread.class);
            printAndLog("Retransformation completed successfully");
        } catch (UnmodifiableClassException e) {
            printAndLogError("Error occurred during retransforming classes" + e.getMessage());
        }
        printAndLog("Transformation and Retransformation finished, " +
                "Thread Agent is working, " +
                "running target application...\n\n\n");
    }
}
