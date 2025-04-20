package org.threadmonitoring.advices;

import net.bytebuddy.asm.Advice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.threadmonitoring.configuration.Configuration;

public final class ThreadStartAdvice {

    public static Logger LOGGER;

    public static void initialize() {
        LOGGER = LogManager.getLogger(ThreadStartAdvice.class);
    }

    @Advice.OnMethodEnter(inline = false)
    public static void interceptEntry(
            @Advice.This Thread thread
    ) {
        StackWalker walker = StackWalker.getInstance();

        boolean foundMonitoredPackage = walker.walk(frames -> frames
                .anyMatch(frame -> Configuration.monitoredPackages.stream()
                        .filter(pkg -> !pkg.equals("java.lang.Object") && !pkg.equals("java.lang.Thread"))
                        .anyMatch(frame.getClassName()::startsWith)));

        if (foundMonitoredPackage) {
            LOGGER.info("Started new thread - {}", thread);
        }
    }
}
