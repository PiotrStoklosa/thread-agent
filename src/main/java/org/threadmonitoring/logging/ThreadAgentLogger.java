package org.threadmonitoring.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.threadmonitoring.substitution.LoggingNotifier.queue;


public class ThreadAgentLogger {

    private static final Map<String, Logger> substitutedClassLoggers = new HashMap<>();

    public static void startLogReader() {
        Thread reader = new Thread(() -> {
            try {
                while (true) {
                    String[] log = queue.take().split("###");
                    String message = log[1];
                    String loggerName = log[2];
                    String logLevel = log[3];
                    substitutedClassLoggers.computeIfAbsent(loggerName, k -> LogManager.getLogger(loggerName))
                            .log(Level.toLevel(logLevel, Level.INFO), message);

                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        // We want to ensure that the application can terminate even if a background task in the Thread Agent is still running.
        reader.setDaemon(true);
        reader.start();
    }

}
