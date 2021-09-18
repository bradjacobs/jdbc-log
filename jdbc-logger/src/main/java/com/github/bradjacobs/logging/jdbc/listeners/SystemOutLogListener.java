package com.github.bradjacobs.logging.jdbc.listeners;

import com.github.bradjacobs.logging.jdbc.LoggingListener;

/**
 * Simple example of a log listener implementation
 *   It's highly recommended using 'real' logger (slf4j, log4j, etc.)
 *      instead of printing to system out.
 */
public class SystemOutLogListener implements LoggingListener
{
    @Override
    public void log(String sql)
    {
        System.out.println("SQL: " + sql);
    }
}
