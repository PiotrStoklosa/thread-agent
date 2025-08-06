package org.threadmonitoring.advices;

import net.bytebuddy.asm.Advice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.threadmonitoring.logging.SynchronizedLogger.logEnter2;
import static org.threadmonitoring.logging.SynchronizedLogger.logExit;


public class SynchronizedMethodAdvice {

    public static Logger LOGGER = LogManager.getLogger(SynchronizedMethodAdvice.class);

    @Advice.OnMethodEnter(inline = false)
    public static void onEnter(@Advice.This(optional = true) Object instance,
                               @Advice.Origin Class<?> clazz) {
        Object monitor = (instance != null) ? instance : clazz;
        logEnter2(monitor);
    }

    @Advice.OnMethodExit(inline = false, onThrowable = Throwable.class)
    public static void onExit(@Advice.This(optional = true) Object instance,
                              @Advice.Origin Class<?> clazz) {
        Object monitor = (instance != null) ? instance : clazz;
        logExit(monitor);
    }
}
