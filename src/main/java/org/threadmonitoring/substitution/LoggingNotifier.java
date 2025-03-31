package org.threadmonitoring.substitution;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LoggingNotifier {

    public static BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public static void log(String message, Class<?> logger, String level) {
        queue.offer(
                new StringBuilder(message)
                        .append("###")
                        .append(logger.toString())
                        .append("###")
                        .append(level)
                        .toString());
    }

}
