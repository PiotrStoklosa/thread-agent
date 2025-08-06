package org.threadmonitoring.advices;

import net.bytebuddy.asm.Advice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.threadmonitoring.analyzer.DeadlockAnalyzer;
import org.threadmonitoring.configuration.Configuration;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

public class UnlockAdvice {

    public static Logger LOGGER;

    public static void initialize() {
        LOGGER = LogManager.getLogger(UnlockAdvice.class);
    }

    private static Optional<StackWalker.StackFrame> findCallingUnlockPlace() {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

        List<StackWalker.StackFrame> frames = walker.walk(stream -> stream.collect(Collectors.toList()));

        for (int i = 0; i < frames.size(); i++) {
            Class<?> cls = frames.get(i).getDeclaringClass();

            if (Lock.class.isAssignableFrom(cls) && frames.get(i).getMethodName().equals("unlock")) {

                List<StackWalker.StackFrame> callers = frames.subList(i + 1, frames.size());

                boolean allFromMonitored = callers.stream().allMatch(f ->
                        Configuration.monitoredPackages.stream()
                                .anyMatch(pkg -> f.getClassName().startsWith(pkg)) ||
                                f.getClassName().equals("java.lang.Thread")
                );

                if (allFromMonitored && !callers.isEmpty()) {
                    return Optional.of(callers.get(0));
                }
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Advice.OnMethodEnter(inline = false)
    public static void interceptEntry(
            @Advice.This Lock lock
    ) {

        Optional<StackWalker.StackFrame> unlockCall = findCallingUnlockPlace();

        if (unlockCall.isPresent()) {
            LOGGER.info("Lock {} released by thread {} at {}",
                    lock.toString(), Thread.currentThread().getName(), unlockCall.get());
        }

    }

    @Advice.OnMethodExit(inline = false)
    public static void interceptExit(
            @Advice.This Lock lock
    ) {
        Optional<StackWalker.StackFrame> unlockCall = findCallingUnlockPlace();

        if (unlockCall.isPresent()) {
            DeadlockAnalyzer.afterReleasingResource(lock);
        }


    }

}