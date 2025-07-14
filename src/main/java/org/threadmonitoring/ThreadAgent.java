package org.threadmonitoring;

import net.bytebuddy.agent.builder.AgentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.threadmonitoring.advices.ExecutorServiceShutdownAdvice;
import org.threadmonitoring.advices.LockAdvice;
import org.threadmonitoring.advices.ThreadConstructorAdvice;
import org.threadmonitoring.advices.ThreadStartAdvice;
import org.threadmonitoring.advices.UnlockAdvice;
import org.threadmonitoring.advices.handler.AdviceHandler;
import org.threadmonitoring.bootstrap.ClassLoadingHandler;
import org.threadmonitoring.configuration.Configuration;
import org.threadmonitoring.logging.ThreadAgentLogger;
import org.threadmonitoring.model.AdviceRule;
import org.threadmonitoring.model.ExecutorServiceModel;
import org.threadmonitoring.model.MethodSubstitutionRule;
import org.threadmonitoring.model.VisitorRule;

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
        ExecutorServiceModel.initialize();
        LockAdvice.initialize();
        UnlockAdvice.initialize();
        ThreadStartAdvice.initialize();
        ExecutorServiceShutdownAdvice.initialize();
    }

    public static void run(Instrumentation inst) {

        printAndLog("Initializing Thread Agent before the target application to enable thread and ExecutorService monitoring");

        printAndLog("The logging has been configured to the " + System.getProperty("log4j2.logdir"));

        Configuration.readConfiguration();

        ThreadAgentLogger.startLogReader();
        initializeClasses();

        printAndLog("Initialized advices");

        Method bootstrap = ClassLoadingHandler.handleClassLoading();
        List<AdviceRule> adviceRules = AdviceHandler.createAdvices();
        List<MethodSubstitutionRule> methodSubstitutionRules = AdviceHandler.createSubstitutions();
        List<VisitorRule> visitorRules = AdviceHandler.createAsmVisitorWrappers();
        AgentBuilder agent = AdviceHandler.buildAgent(adviceRules,
                methodSubstitutionRules, visitorRules, bootstrap);


        agent.installOn(inst);
        printAndLog("Advices and method substitutions created and installed");

        try {
            printAndLog("Attempting to retransform classes");
            for (Class<?> clazz : inst.getAllLoadedClasses()) {
                if (java.util.concurrent.ExecutorService.class.isAssignableFrom(clazz)) {
                    inst.retransformClasses(clazz);
                }
            }
            inst.retransformClasses(Thread.class);
            inst.retransformClasses(Object.class);
            printAndLog("Retransformation completed successfully");
        } catch (UnmodifiableClassException e) {
            printAndLogError("Error occurred during retransforming classes" + e.getMessage());
        }
        Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
        printAndLog("Transformation and Retransformation finished, " +
                "Thread Agent is working, " +
                "running target application...\n\n\n");
    }
}
