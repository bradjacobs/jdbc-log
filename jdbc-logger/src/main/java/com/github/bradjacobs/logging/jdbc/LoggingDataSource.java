package com.github.bradjacobs.logging.jdbc;

import com.github.bradjacobs.logging.jdbc.listeners.LoggingListener;
import com.github.bradjacobs.logging.jdbc.listeners.Slf4jLoggingListener;

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
    private boolean enabled = true;

    /**
     * Constructor (convenience), wraps datasource and will log SQL statements to logger (debug level)
     * @param dataSource dataSource
     * @param logger logger
     */
    public LoggingDataSource(DataSource dataSource, org.slf4j.Logger logger) {
        this(dataSource, new Slf4jLoggingListener(logger));
    }

    /**
     * Constructor wraps existing dataSource and will log SQL statements to the listeners.
     * @param dataSource dataSource
     * @param loggingListeners loggingListeners
     */
    public LoggingDataSource(DataSource dataSource, LoggingListener... loggingListeners)
    {
        this(dataSource, LoggingConnectionCreator.buildDefault(loggingListeners));
    }


    /**
     * Constructor to use any customized loggingConnectionCreator.
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
    public Connection getConnection() throws SQLException {
        Connection innerConnection = dataSource.getConnection();
        return createConnection(innerConnection);
    }

    /** @inheritDoc */
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection innerConnection = dataSource.getConnection(username, password);
        return createConnection(innerConnection);
    }

    /**
     * Creates a new LoggingConnection.
     *   if 'disabled', then will just get back the passed in connection.
     * @param innerConnection connection
     * @return loggingConnection.
     */
    private Connection createConnection(Connection innerConnection) {
        if (!enabled) {
            return innerConnection;
        }
        return loggingConnectionCreator.create(innerConnection);
    }


    /**
     *  Returns if Sql Connection Logging is enabled.
     *  A 'false' means logging disabled and calls to 'getConnection'
     *  will return the original Connection instead of a LoggingConnection
     * @return isEnabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables SQL logging
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    private void validateParams(DataSource dataSource, LoggingConnectionCreator loggingConnectionCreator)
            throws IllegalArgumentException
    {
        if (dataSource == null) {
            throw new IllegalArgumentException("Must provide a dateSource");
        }
        if (loggingConnectionCreator == null) {
            throw new IllegalArgumentException("Must provide a loggingConnectionCreator");
        }
    }
}
