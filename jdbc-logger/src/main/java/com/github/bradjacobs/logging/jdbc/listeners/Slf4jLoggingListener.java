package com.github.bradjacobs.logging.jdbc.listeners;

import org.slf4j.Logger;
import org.slf4j.event.Level;

/**
 * Simple implemenation of Logging Listener for SLF4J
 */
public class Slf4jLoggingListener implements LoggingListener
{
    private static final Level DEFAULT_LOG_LEVEL = Level.DEBUG;

    private final Logger logger;
    private final Level logLevel;

    public Slf4jLoggingListener(Logger logger)
    {
        this(logger, DEFAULT_LOG_LEVEL);
    }

    public Slf4jLoggingListener(Logger logger, Level logLevel)
    {
        if (logger == null) {
            throw new IllegalArgumentException("Must provide a non-null logger.");
        }
        this.logger = logger;
        this.logLevel = (logLevel != null ? logLevel : DEFAULT_LOG_LEVEL);
    }

    @Override
    public void log(String sql) {

        switch (logLevel)
        {
            case DEBUG:
                if (logger.isDebugEnabled()) { logger.debug(sql); }
                break;
            case TRACE:
                if (logger.isTraceEnabled()) { logger.trace(sql); }
                break;
            case INFO:
                if (logger.isInfoEnabled()) { logger.info(sql); }
                break;
            case WARN:
                if (logger.isWarnEnabled()) { logger.warn(sql); }
                break;
            case ERROR:
                if (logger.isErrorEnabled()) { logger.error(sql); }
                break;
        }
    }
}
