package org.threadmonitoring;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LoggingNotifier {

    public static BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public static void log(String message) {
        queue.offer(message);
    }

}
