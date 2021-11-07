package com.github.bradjacobs.logging.jdbc.listeners;


import java.util.logging.Level;
import java.util.logging.Logger;

public class JDKLoggingListener implements LoggingListener
{
    private static final Level DEFAULT_LOG_LEVEL = Level.FINE;

    private final Logger logger;
    private final Level logLevel;

    public JDKLoggingListener(Logger logger) {
        this(logger, DEFAULT_LOG_LEVEL);
    }

    public JDKLoggingListener(Logger logger, Level logLevel) {
        if (logger == null) {
            throw new IllegalArgumentException("Must provide a non-null logger.");
        }
        this.logger = logger;
        this.logLevel = (logLevel != null ? logLevel : DEFAULT_LOG_LEVEL);
    }

    @Override
    public void log(String sql) {
        logger.log(logLevel, sql);
    }
}
