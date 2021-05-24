package bwj.logging.jdbc;

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
    private final LoggingConnectionBuilder loggingConnectionBuilder;

    private boolean loggingEnabled = true;


    /**
     * Simple Constructor uses all the "defaults" for SQL Database logging.
     * @param dataSource dateSource
     */
    public LoggingDataSource(DataSource dataSource)
    {
        this(dataSource, new LoggingConnectionBuilder());
    }

    /**
     * Custom constructor that can pass in own builder that has been 'preset' accordingly
     * @param dataSource dateSource
     * @param loggingConnectionBuilder loggingConnectionBuilder
     */
    public LoggingDataSource(DataSource dataSource, LoggingConnectionBuilder loggingConnectionBuilder)
    {
        validateParams(dataSource, loggingConnectionBuilder);
        this.dataSource = dataSource;
        this.loggingConnectionBuilder = loggingConnectionBuilder;
    }


    /** @inheritDoc */
    @Override
    public Connection getConnection() throws SQLException
    {
        Connection innerConnection = dataSource.getConnection();
        if (!loggingEnabled) {
            return innerConnection;
        }
        return loggingConnectionBuilder.build(innerConnection);
    }

    /** @inheritDoc */
    @Override
    public Connection getConnection(String username, String password) throws SQLException
    {
        Connection innerConnection = dataSource.getConnection(username, password);
        if (!loggingEnabled) {
            return innerConnection;
        }
        return loggingConnectionBuilder.build(innerConnection);
    }

    /**
     * Check if LoggingConnection is enabled.
     *  a 'false' means logging disabled and calls to 'getConnection'
     *  will return the original Connection instead of a LoggingConnection
     * @return isEnabled.
     */
    public boolean isLoggingEnabled()
    {
        return loggingEnabled;
    }

    /**
     * Enables usage of logging connections
     *   default: true
     */
    public void setLoggingEnabled(boolean loggingEnabled)
    {
        this.loggingEnabled = loggingEnabled;
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


    private void validateParams(DataSource dataSource, LoggingConnectionBuilder loggingConnectionBuilder) throws IllegalArgumentException
    {
        if (dataSource == null) {
            throw new IllegalArgumentException("Must provide a dateSource");
        }
        if (loggingConnectionBuilder == null) {
            throw new IllegalArgumentException("Must provide a loggingConnectionBuilder");
        }
    }

}
