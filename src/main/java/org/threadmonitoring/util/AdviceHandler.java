package org.threadmonitoring.util;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import org.threadmonitoring.ThreadAgent;
import org.threadmonitoring.advices.*;
import org.threadmonitoring.model.AdviceRule;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class AdviceHandler {

    public static AgentBuilder buildAgentWithAdvices(List<AdviceRule> adviceRules, Method bootstrapMethod) {
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
                .with(AgentBuilder.TypeStrategy.Default.REBASE);
    }


    public static List<AdviceRule> createAdvices() {
        return List.of(
                new AdviceRule.Builder()
                        .setTypeMatcher(is(Thread.class))
                        .setMethodMatcher(isConstructor())
                        .setClassName(ThreadConstructorAdvice.class.getName())
                        .build()
                ,
                new AdviceRule.Builder()
                        .setTypeMatcher(isSubTypeOf(Lock.class))
                        .setMethodMatcher(isMethod().and(ElementMatchers.named("lock")))
                        .setClassName(LockAdvice.class.getName())
                        .build()
                ,
                new AdviceRule.Builder()
                        .setTypeMatcher(isSubTypeOf(Lock.class))
                        .setMethodMatcher(isMethod().and(ElementMatchers.named("unlock")))
                        .setClassName(UnlockAdvice.class.getName())
                        .build()
                ,
                new AdviceRule.Builder()
                        .setTypeMatcher(isSubTypeOf(Executor.class))
                        .setMethodMatcher(isMethod().and(ElementMatchers.named("shutdown")))
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
                        .setMethodMatcher(isMethod().and(ElementMatchers.named("start")))
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
                        .setMethodMatcher(isMethod().and(ElementMatchers.named("execute").or(named("submit"))))
                        .setClassName(ExecutorExecuteSubmitAdvice.class.getName())
                        .build()
        );
    }

}
