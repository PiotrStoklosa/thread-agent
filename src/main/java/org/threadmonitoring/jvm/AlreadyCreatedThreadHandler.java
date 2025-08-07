package org.threadmonitoring.jvm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public class AlreadyCreatedThreadHandler {

    public static Logger LOGGER = LogManager.getLogger(AlreadyCreatedThreadHandler.class);

    public static void logAlreadyCreatedThreads() {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        for (Thread thread : threadSet) {
            LOGGER.info("Thread {} has already been created by JVM", thread.getName());
        }
    }

}
