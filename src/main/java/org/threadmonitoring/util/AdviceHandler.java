package org.threadmonitoring.util;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.MemberSubstitution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.threadmonitoring.ThreadAgent;
import org.threadmonitoring.advices.ExecutorConstructorAdvice;
import org.threadmonitoring.advices.ExecutorExecuteSubmitAdvice;
import org.threadmonitoring.advices.ExecutorShutdownAdvice;
import org.threadmonitoring.advices.LockAdvice;
import org.threadmonitoring.advices.ThreadConstructorAdvice;
import org.threadmonitoring.advices.ThreadStartAdvice;
import org.threadmonitoring.advices.UnlockAdvice;
import org.threadmonitoring.model.AdviceRule;
import org.threadmonitoring.model.MethodSubstitutionRule;
import org.threadmonitoring.model.MethodTemplate;
import org.threadmonitoring.substitution.NotifySubstitution;
import org.threadmonitoring.substitution.SleepSubstitution;
import org.threadmonitoring.substitution.WaitSubstitution;
import org.threadmonitoring.substitution.YieldSubstitution;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;

import static net.bytebuddy.matcher.ElementMatchers.any;
import static net.bytebuddy.matcher.ElementMatchers.is;
import static net.bytebuddy.matcher.ElementMatchers.isConstructor;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isSubTypeOf;
import static net.bytebuddy.matcher.ElementMatchers.nameContains;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.none;
import static net.bytebuddy.matcher.ElementMatchers.returns;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;
import static net.bytebuddy.matcher.ElementMatchers.takesNoArguments;

public class AdviceHandler {

    private static final Logger LOGGER = LogManager.getLogger(AdviceHandler.class);


    public static AgentBuilder buildAgentWithAdvicesAndSubstitutions(List<AdviceRule> adviceRules, List<MethodSubstitutionRule> methodSubstitutionRules, Method bootstrapMethod) {
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
                        .setTypeMatcher(isSubTypeOf(Executor.class))
                        .setMethodMatcher(isMethod().and(named("shutdown")))
                        .setClassName(ExecutorShutdownAdvice.class.getName())
                        .build()
/*                ,
                new AdviceRule.Builder()
                        .setTypeMatcher(any())
                        .setMethodMatcher(isMethod().and(ElementMatchers.named("main")
                                        .and(takesArguments(String[].class))
                                        .and(isPublic())
                                       .and(isStatic())))
                        .setClassName(ThreadStatusScanner.class.getName())
                        .build()*/
                ,
                new AdviceRule.Builder()
                        .setTypeMatcher(isSubTypeOf(Thread.class))
                        .setMethodMatcher(isMethod().and(named("start")))
                        .setClassName(ThreadStartAdvice.class.getName())
                        .build()
                ,
                new AdviceRule.Builder()
                        .setTypeMatcher(isSubTypeOf(Executor.class))
                        .setMethodMatcher(isConstructor())
                        .setClassName(ExecutorConstructorAdvice.class.getName())
                        .build()
                ,
                new AdviceRule.Builder()
                        .setTypeMatcher(isSubTypeOf(Executor.class))
                        .setMethodMatcher(isMethod().and(named("execute").or(named("submit"))))
                        .setClassName(ExecutorExecuteSubmitAdvice.class.getName())
                        .build()
        );
    }

    public static List<MethodSubstitutionRule> createSubstitutions() {
        return List.of(new MethodSubstitutionRule.Builder()
                        .setTypeMatcher(nameContains("org.example").or(named("java.lang.Object")))
                        .setSubstituteMethod(named("sleep")
                                .and(takesArguments(long.class))
                                .and(returns(void.class)))
                        .setNewMethod(new MethodTemplate.Builder()
                                .setClazz(SleepSubstitution.class)
                                .setMethodName("sleep2")
                                .setArguments(List.of(long.class))
                                .build())
                        .build()
                , new MethodSubstitutionRule.Builder()
                        .setTypeMatcher(nameContains("org.example").or(named("java.lang.Object")))
                        .setSubstituteMethod(named("notify")
                                .and(returns(void.class))
                                .and(takesNoArguments()))
                        .setNewMethod(new MethodTemplate.Builder()
                                .setClazz(NotifySubstitution.class)
                                .setMethodName("notify2")
                                .setArguments(List.of(Object.class))
                                .build())
                        .build()
                , new MethodSubstitutionRule.Builder()
                        .setTypeMatcher(nameContains("org.example").or(named("java.lang.Object")))
                        .setSubstituteMethod(named("wait")
                                .and(returns(void.class))
                                .and(takesNoArguments()))
                        .setNewMethod(new MethodTemplate.Builder()
                                .setClazz(WaitSubstitution.class)
                                .setMethodName("wait2")
                                .setArguments(List.of(Object.class))
                                .build())
                        .build(),
                new MethodSubstitutionRule.Builder()
                        .setTypeMatcher(nameContains("org.example").or(named("java.lang.Object")))
                        .setSubstituteMethod(named("yield")
                                .and(returns(void.class))
                                .and(takesNoArguments()))
                        .setNewMethod(new MethodTemplate.Builder()
                                .setClazz(YieldSubstitution.class)
                                .setMethodName("yield2")
                                .setArguments(List.of())
                                .build())
                        .build());
    }
}
