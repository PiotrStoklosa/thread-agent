package org.threadmonitoring.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.config.Configuration;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EmergencyLogger {

    private static final String LOG_FILE = initPath();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    private static String initPath() {
        String mainLog = getLog4jLogFilePath();
        if (mainLog == null) return "emergency.log";
        return mainLog.replace(".log", "_emergency.log");
    }

    public static synchronized void log(String message) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            fw.write("[" + LocalDateTime.now().format(formatter) + "] " + message + "\n");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static String getLog4jLogFilePath() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        RollingFileAppender appender = config.getAppender("RollingFileAppender");
        return appender != null ? appender.getFileName() : null;
    }
}
