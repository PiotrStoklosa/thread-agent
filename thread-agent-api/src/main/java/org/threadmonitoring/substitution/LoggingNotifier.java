package org.threadmonitoring.substitution;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class LoggingNotifier {

    private static final AtomicLong id = new AtomicLong(0);
    public static BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    public static BlockingQueue<String> emergencyLoggingQueue = new LinkedBlockingQueue<>();

    public static synchronized void log(String message, Class<?> logger, String level) {
        long freshId = id.getAndIncrement();
        String seq = String.format("%020d", freshId);
        queue.offer(
                new StringBuilder(seq)
                        .append("###")
                        .append(message)
                        .append("###")
                        .append(logger.toString())
                        .append("###")
                        .append(level)
                        .toString()
        );
    }

    public static synchronized void emergencyLog(String message) {
        emergencyLoggingQueue.offer(message);
    }

}
