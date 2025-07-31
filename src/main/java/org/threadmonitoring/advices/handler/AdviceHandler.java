package org.threadmonitoring.advices.handler;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.MemberSubstitution;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.threadmonitoring.ThreadAgent;
import org.threadmonitoring.advices.ExecutorServiceConstructorAdvice;
import org.threadmonitoring.advices.ExecutorServiceExecuteSubmitAdvice;
import org.threadmonitoring.advices.ExecutorServiceShutdownAdvice;
import org.threadmonitoring.advices.LockAdvice;
import org.threadmonitoring.advices.SynchronizedMethodAdvice;
import org.threadmonitoring.advices.ThreadConstructorAdvice;
import org.threadmonitoring.advices.ThreadStartAdvice;
import org.threadmonitoring.advices.UnlockAdvice;
import org.threadmonitoring.advices.wrapper.MonitorEnterExitSynchronizedBlockWrapper;
import org.threadmonitoring.jvm.DestroyJVMMonitor;
import org.threadmonitoring.model.AdviceRule;
import org.threadmonitoring.model.MethodSubstitutionRule;
import org.threadmonitoring.model.MethodTemplate;
import org.threadmonitoring.model.VisitorRule;
import org.threadmonitoring.substitution.ConditionSubstitution;
import org.threadmonitoring.substitution.NotifyAllSubstitution;
import org.threadmonitoring.substitution.NotifySubstitution;
import org.threadmonitoring.substitution.SleepSubstitution;
import org.threadmonitoring.substitution.WaitSubstitution;
import org.threadmonitoring.substitution.YieldSubstitution;
import org.threadmonitoring.substitution.call.GeneralSubstitution;
import org.threadmonitoring.substitution.call.SynchronizedCall;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import static net.bytebuddy.matcher.ElementMatchers.any;
import static net.bytebuddy.matcher.ElementMatchers.is;
import static net.bytebuddy.matcher.ElementMatchers.isConstructor;
import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isSubTypeOf;
import static net.bytebuddy.matcher.ElementMatchers.isSynchronized;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.none;
import static net.bytebuddy.matcher.ElementMatchers.not;
import static net.bytebuddy.matcher.ElementMatchers.returns;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;
import static net.bytebuddy.matcher.ElementMatchers.takesNoArguments;

public class AdviceHandler {

    private static final Logger LOGGER = LogManager.getLogger(AdviceHandler.class);
    public static ElementMatcher.Junction<TypeDescription> matcher = none();

    public static AgentBuilder buildAgent(List<AdviceRule> adviceRules, List<MethodSubstitutionRule> methodSubstitutionRules, List<VisitorRule> visitorRules, Method bootstrapMethod) {

        DestroyJVMMonitor.displayAllThreads();

        AgentBuilder agentBuilder = buildAgent();

        for (AdviceRule adviceRule : adviceRules) {
            agentBuilder = agentBuilder
                    .type(adviceRule.getTypeMatcher())
                    .transform(
                            new AgentBuilder.Transformer.ForAdvice(Advice.withCustomMapping().bootstrap(bootstrapMethod))
                                    .include(ThreadAgent.class.getClassLoader())
                                    .advice(adviceRule.getMethodMatcher(), adviceRule.getClassName()
                                    ));
        }


        MethodTemplate newMethodTemplate;

        for (MethodSubstitutionRule methodSubstitutionRule : methodSubstitutionRules) {

            newMethodTemplate = methodSubstitutionRule.getNewMethod();
            Method newMethod = null;

            try {
                newMethod = newMethodTemplate.getClazz().getMethod(newMethodTemplate.getMethodName(),
                        newMethodTemplate.getArguments().toArray(new Class<?>[0]));
            } catch (NoSuchMethodException e) {
                LOGGER.error("Didn't find method {}", newMethodTemplate.getMethodName());
            }

            Method finalNewMethod = newMethod;
            agentBuilder = agentBuilder
                    .type(methodSubstitutionRule.getTypeMatcher())
                    .transform((builder, typeDescription, classLoader, module, isRedefinition) ->
                            builder.visit(MemberSubstitution.relaxed()
                                    .method(methodSubstitutionRule.getSubstituteMethod()
                                    )
                                    .replaceWith(finalNewMethod)
                                    .failIfNoMatch(false)
                                    .on(any())));
        }

        for (VisitorRule visitorRule : visitorRules) {
            agentBuilder = agentBuilder
                    .type(visitorRule.getTypeMatcher())
                    .transform((builder, typeDescription, classLoader, module, isRedefinition) ->
                            builder.visit(visitorRule.getAsmVisitorWrapper()));
        }

        return agentBuilder;
    }

    public static AgentBuilder buildAgent() {
        return new AgentBuilder
                .Default()
                .ignore(none())
                .disableClassFormatChanges()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(AgentBuilder.RedefinitionStrategy.DiscoveryStrategy.Reiterating.INSTANCE)
                .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
/*                .with(AgentBuilder.Listener.StreamWriting.toSystemError())
                .with(AgentBuilder.InstallationListener.StreamWriting.toSystemError())*/
                .with(AgentBuilder.TypeStrategy.Default.DECORATE)
                .with(AgentBuilder.LambdaInstrumentationStrategy.ENABLED);
    }

    public static List<AdviceRule> createAdvices() {
        return List.of(
                new AdviceRule.Builder()
                        .setTypeMatcher(is(Thread.class))
                        .setMethodMatcher(isConstructor().and(takesArguments(6)))
                        .setClassName(ThreadConstructorAdvice.class.getName())
                        .build()
                ,
                new AdviceRule.Builder()
                        .setTypeMatcher(isSubTypeOf(Lock.class))
                        .setMethodMatcher(isMethod().and(named("lock")))
                        .setClassName(LockAdvice.class.getName())
                        .build()
                ,
                new AdviceRule.Builder()
                        .setTypeMatcher(isSubTypeOf(Lock.class))
                        .setMethodMatcher(isMethod().and(named("unlock")))
                        .setClassName(UnlockAdvice.class.getName())
                        .build()
                ,
                new AdviceRule.Builder()
                        .setTypeMatcher(isSubTypeOf(ExecutorService.class))
                        .setMethodMatcher(isMethod().and(named("shutdown")))
                        .setClassName(ExecutorServiceShutdownAdvice.class.getName())
                        .build()
                ,
                new AdviceRule.Builder()
                        .setTypeMatcher(isSubTypeOf(Thread.class))
                        .setMethodMatcher(isMethod().and(named("start")))
                        .setClassName(ThreadStartAdvice.class.getName())
                        .build()
                ,
                new AdviceRule.Builder()
                        .setTypeMatcher(isSubTypeOf(ExecutorService.class))
                        .setMethodMatcher(isConstructor())
                        .setClassName(ExecutorServiceConstructorAdvice.class.getName())
                        .build()
                ,
                new AdviceRule.Builder()
                        .setTypeMatcher(isSubTypeOf(ExecutorService.class))
                        .setMethodMatcher(isMethod().and(named("execute").or(named("submit"))))
                        .setClassName(ExecutorServiceExecuteSubmitAdvice.class.getName())
                        .build()
                ,
                new AdviceRule.Builder()
                        .setTypeMatcher(matcher)
                        .setMethodMatcher(isSynchronized())
                        .setClassName(SynchronizedMethodAdvice.class.getName())
                        .build()
        );
    }

    public static List<MethodSubstitutionRule> createSubstitutions() {
        return List.of(new MethodSubstitutionRule.Builder()
                        .setTypeMatcher(matcher)
                        .setSubstituteMethod(named("sleep")
                                .and(takesArguments(long.class))
                                .and(returns(void.class))
                                .and(isDeclaredBy(Thread.class)))
                        .setNewMethod(new MethodTemplate.Builder()
                                .setClazz(SleepSubstitution.class)
                                .setMethodName("sleep2")
                                .setArguments(List.of(long.class))
                                .build())
                        .build()
                , new MethodSubstitutionRule.Builder()
                        .setTypeMatcher(matcher)
                        .setSubstituteMethod(named("notify")
                                .and(returns(void.class))
                                .and(takesNoArguments())
                                .and(isDeclaredBy(Object.class)))
                        .setNewMethod(new MethodTemplate.Builder()
                                .setClazz(NotifySubstitution.class)
                                .setMethodName("notify2")
                                .setArguments(List.of(Object.class))
                                .build())
                        .build()
                , new MethodSubstitutionRule.Builder()
                        .setTypeMatcher(matcher)
                        .setSubstituteMethod(named("notifyAll")
                                .and(returns(void.class))
                                .and(takesNoArguments())
                                .and(isDeclaredBy(Object.class)))
                        .setNewMethod(new MethodTemplate.Builder()
                                .setClazz(NotifyAllSubstitution.class)
                                .setMethodName("notifyAll2")
                                .setArguments(List.of(Object.class))
                                .build())
                        .build()
                , new MethodSubstitutionRule.Builder()
                        .setTypeMatcher(matcher)
                        .setSubstituteMethod(named("wait")
                                .and(returns(void.class))
                                .and(takesNoArguments())
                                .and(isDeclaredBy(Object.class)))
                        .setNewMethod(new MethodTemplate.Builder()
                                .setClazz(WaitSubstitution.class)
                                .setMethodName("wait2")
                                .setArguments(List.of(Object.class))
                                .build())
                        .build()
                , new MethodSubstitutionRule.Builder()
                        .setTypeMatcher(matcher)
                        .setSubstituteMethod(named("alertBeforeSynchronizedEntry")
                                .and(returns(void.class))
                                .and(takesArguments(Object.class)))
                        .setNewMethod(new MethodTemplate.Builder()
                                .setClazz(SynchronizedCall.class)
                                .setMethodName("alertBeforeSynchronizedEntry2")
                                .setArguments(List.of(Object.class))
                                .build())
                        .build()
                , new MethodSubstitutionRule.Builder()
                        .setTypeMatcher(matcher)
                        .setSubstituteMethod(named("alertSynchronizedEntry")
                                .and(returns(void.class))
                                .and(takesArguments(Object.class)))
                        .setNewMethod(new MethodTemplate.Builder()
                                .setClazz(SynchronizedCall.class)
                                .setMethodName("alertSynchronizedEntry2")
                                .setArguments(List.of(Object.class))
                                .build())
                        .build()
                , new MethodSubstitutionRule.Builder()
                        .setTypeMatcher(matcher)
                        .setSubstituteMethod(named("alertSynchronizedExit")
                                .and(returns(void.class))
                                .and(takesArguments(Object.class)))
                        .setNewMethod(new MethodTemplate.Builder()
                                .setClazz(SynchronizedCall.class)
                                .setMethodName("alertSynchronizedExit2")
                                .setArguments(List.of(Object.class))
                                .build())
                        .build()
                , new MethodSubstitutionRule.Builder()
                        .setTypeMatcher(matcher)
                        .setSubstituteMethod(named("alertMultithreadingCall")
                                .and(returns(void.class))
                                .and(takesArguments(String.class)))
                        .setNewMethod(new MethodTemplate.Builder()
                                .setClazz(GeneralSubstitution.class)
                                .setMethodName("substitute2")
                                .setArguments(List.of(String.class))
                                .build())
                        .build()
                , new MethodSubstitutionRule.Builder()
                        .setTypeMatcher(matcher)
                        .setSubstituteMethod(named("signal")
                                .and(returns(void.class))
                                .and(takesArguments(0))
                                .and(isDeclaredBy(Condition.class)))
                        .setNewMethod(new MethodTemplate.Builder()
                                .setClazz(ConditionSubstitution.class)
                                .setMethodName("signal2")
                                .setArguments(List.of(Condition.class))
                                .build())
                        .build()
                , new MethodSubstitutionRule.Builder()
                        .setTypeMatcher(matcher)
                        .setSubstituteMethod(named("signalAll")
                                .and(returns(void.class))
                                .and(takesArguments(0))
                                .and(isDeclaredBy(Condition.class)))
                        .setNewMethod(new MethodTemplate.Builder()
                                .setClazz(ConditionSubstitution.class)
                                .setMethodName("signalAll2")
                                .setArguments(List.of(Condition.class))
                                .build())
                        .build()
                , new MethodSubstitutionRule.Builder()
                        .setTypeMatcher(matcher)
                        .setSubstituteMethod(named("awaitUninterruptibly")
                                .and(returns(void.class))
                                .and(takesArguments(0))
                                .and(isDeclaredBy(Condition.class)))
                        .setNewMethod(new MethodTemplate.Builder()
                                .setClazz(ConditionSubstitution.class)
                                .setMethodName("awaitUninterruptibly2")
                                .setArguments(List.of(Condition.class))
                                .build())
                        .build()
                , new MethodSubstitutionRule.Builder()
                        .setTypeMatcher(matcher)
                        .setSubstituteMethod(named("await")
                                .and(returns(void.class))
                                .and(takesArguments(0))
                                .and(isDeclaredBy(Condition.class)))
                        .setNewMethod(new MethodTemplate.Builder()
                                .setClazz(ConditionSubstitution.class)
                                .setMethodName("await2")
                                .setArguments(List.of(Condition.class))
                                .build())
                        .build()
                , new MethodSubstitutionRule.Builder()
                        .setTypeMatcher(matcher)
                        .setSubstituteMethod(named("awaitNanos")
                                .and(returns(long.class))
                                .and(takesArguments(1))
                                .and(isDeclaredBy(Condition.class)))
                        .setNewMethod(new MethodTemplate.Builder()
                                .setClazz(ConditionSubstitution.class)
                                .setMethodName("awaitNanos2")
                                .setArguments(List.of(Condition.class, long.class))
                                .build())
                        .build()
                , new MethodSubstitutionRule.Builder()
                        .setTypeMatcher(matcher)
                        .setSubstituteMethod(named("await")
                                .and(returns(boolean.class))
                                .and(takesArguments(2))
                                .and(isDeclaredBy(Condition.class)))
                        .setNewMethod(new MethodTemplate.Builder()
                                .setClazz(ConditionSubstitution.class)
                                .setMethodName("await2")
                                .setArguments(List.of(Condition.class, long.class, TimeUnit.class))
                                .build())
                        .build()
                , new MethodSubstitutionRule.Builder()
                        .setTypeMatcher(matcher)
                        .setSubstituteMethod(named("awaitUntil")
                                .and(returns(boolean.class))
                                .and(takesArguments(1))
                                .and(isDeclaredBy(Condition.class)))
                        .setNewMethod(new MethodTemplate.Builder()
                                .setClazz(ConditionSubstitution.class)
                                .setMethodName("awaitUntil2")
                                .setArguments(List.of(Condition.class, Date.class))
                                .build())
                        .build()
                , new MethodSubstitutionRule.Builder()
                        .setTypeMatcher(matcher)
                        .setSubstituteMethod(named("yield")
                                .and(returns(void.class))
                                .and(takesNoArguments())
                                .and(isDeclaredBy(Thread.class)))
                        .setNewMethod(new MethodTemplate.Builder()
                                .setClazz(YieldSubstitution.class)
                                .setMethodName("yield2")
                                .setArguments(List.of())
                                .build())
                        .build());
    }

    public static List<VisitorRule> createAsmVisitorWrappers() {
        ElementMatcher.Junction<TypeDescription> asmVisitorWrapperMatcher = matcher.and(not(
                nameStartsWith("sun.")
                        .or(nameStartsWith("com.sun."))
                        .or(nameStartsWith("jdk."))
                        .or(nameStartsWith("java.instrument."))
                        .or(nameStartsWith("java.util."))
                        .or(nameStartsWith("javax."))
                        .or(nameStartsWith("java.lang."))
        ));
        return List.of(
                new VisitorRule.Builder()
                        .setTypeMatcher(asmVisitorWrapperMatcher)
                        .setAsmVisitorWrapper(new MonitorEnterExitSynchronizedBlockWrapper())
                        .build()
        );
    }
}
