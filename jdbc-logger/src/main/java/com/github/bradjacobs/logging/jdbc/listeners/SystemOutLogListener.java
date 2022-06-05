package com.github.bradjacobs.logging.jdbc.listeners;

/**
 * Simple example of a log listener implementation
 *   It's highly recommended using 'real' logger such as slf4j
 *      instead of printing to system out.
 */
public class SystemOutLogListener implements LoggingListener {
    @Override
    public void log(String sql)
    {
        System.out.println(sql);
    }
}
