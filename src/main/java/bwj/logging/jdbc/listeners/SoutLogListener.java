package bwj.logging.jdbc.listeners;

import bwj.logging.jdbc.LoggingListener;

/**
 * Simple example of a log listner implementation
 *  It's recommended to use a 'real' logger like (slf4j, log4j, etc, etc) instead of printing to system out.
 */
public class SoutLogListener implements LoggingListener
{
    @Override
    public void log(String sql)
    {
        System.out.println("** SQL: " + sql);
    }
}
