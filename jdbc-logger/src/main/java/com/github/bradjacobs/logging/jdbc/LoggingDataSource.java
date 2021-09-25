package com.github.bradjacobs.logging.jdbc;

import com.github.bradjacobs.logging.jdbc.listeners.LoggingListener;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;


/**
 *
 * @see <a href="https://docs.spring.io/spring-boot/docs/1.5.14.RELEASE/reference/html/howto-data-access.html">Spring - Configure a Custom DataSource</a>
 */
public class LoggingDataSource implements DataSource
{
    private final DataSource dataSource;
    private final LoggingConnectionCreator loggingConnectionCreator;


    /**
     *
     * @param dataSource dataSource
     * @param loggingListener loggingListener
     */
    public LoggingDataSource(DataSource dataSource, LoggingListener... loggingListener)
    {
        this(dataSource, LoggingConnectionCreator.builder().withLogListener(loggingListener).build());
    }


    /**
     * Custom constructor that can in custom config
     * @param dataSource dateSource
     * @param loggingConnectionCreator loggingConnectionCreator
     */
    public LoggingDataSource(DataSource dataSource, LoggingConnectionCreator loggingConnectionCreator)
    {
        validateParams(dataSource, loggingConnectionCreator);
        this.dataSource = dataSource;
        this.loggingConnectionCreator = loggingConnectionCreator;
    }


    /** @inheritDoc */
    @Override
    public Connection getConnection() throws SQLException
    {
        Connection innerConnection = dataSource.getConnection();
        return loggingConnectionCreator.getConnection(innerConnection);
    }

    /** @inheritDoc */
    @Override
    public Connection getConnection(String username, String password) throws SQLException
    {
        Connection innerConnection = dataSource.getConnection(username, password);
        return loggingConnectionCreator.getConnection(innerConnection);
    }

    /**
     * Check if LoggingConnection is enabled.
     *  a 'false' means logging disabled and calls to 'getConnection'
     *  will return the original Connection instead of a LoggingConnection
     * @return isEnabled.
     */
    public boolean isLoggingEnabled()
    {
        return loggingConnectionCreator.isLoggingEnabled();
    }

    /**
     * Enables logging
     */
    public void setLoggingEnabled(boolean loggingEnabled)
    {
        this.loggingConnectionCreator.setLoggingEnabled(loggingEnabled);
    }


    /** @inheritDoc */
    @Override
    public PrintWriter getLogWriter() throws SQLException
    {
        return dataSource.getLogWriter();
    }

    /** @inheritDoc */
    @Override
    public void setLogWriter(PrintWriter out) throws SQLException
    {
        dataSource.setLogWriter(out);
    }

    /** @inheritDoc */
    @Override
    public void setLoginTimeout(int seconds) throws SQLException
    {
        dataSource.setLoginTimeout(seconds);
    }

    /** @inheritDoc */
    @Override
    public int getLoginTimeout() throws SQLException
    {
        return dataSource.getLoginTimeout();
    }

    /** @inheritDoc */
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        return dataSource.getParentLogger();
    }
    
    /** @inheritDoc */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        return dataSource.unwrap(iface);
    }

    /** @inheritDoc */
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return dataSource.isWrapperFor(iface);
    }


    private void validateParams(DataSource dataSource, LoggingConnectionCreator loggingConnectionCreator) throws IllegalArgumentException
    {
        if (dataSource == null) {
            throw new IllegalArgumentException("Must provide a dateSource");
        }
        if (loggingConnectionCreator == null) {
            throw new IllegalArgumentException("Must provide a loggingConnectionCreator");
        }
    }

}
