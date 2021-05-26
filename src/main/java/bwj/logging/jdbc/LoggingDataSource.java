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
    private final LoggingConnectionFactory loggingConnectionFactory;

    /**
     * Custom constructor that can pass in own builder that has been 'preset' accordingly
     * @param dataSource dateSource
     * @param loggingSqlConfig loggingSqlConfig
     */
    public LoggingDataSource(DataSource dataSource, LoggingSqlConfig loggingSqlConfig)
    {
        validateParams(dataSource, loggingSqlConfig);
        this.dataSource = dataSource;
        this.loggingConnectionFactory = new LoggingConnectionFactory(loggingSqlConfig);
    }


    /** @inheritDoc */
    @Override
    public Connection getConnection() throws SQLException
    {
        Connection innerConnection = dataSource.getConnection();
        return loggingConnectionFactory.getConnection(innerConnection);
    }

    /** @inheritDoc */
    @Override
    public Connection getConnection(String username, String password) throws SQLException
    {
        Connection innerConnection = dataSource.getConnection(username, password);
        return loggingConnectionFactory.getConnection(innerConnection);
    }

    /**
     * Check if LoggingConnection is enabled.
     *  a 'false' means logging disabled and calls to 'getConnection'
     *  will return the original Connection instead of a LoggingConnection
     * @return isEnabled.
     */
    public boolean isLoggingEnabled()
    {
        return loggingConnectionFactory.isLoggingEnabled();
    }

    /**
     * Enables usage of logging connections
     */
    public void setLoggingEnabled(boolean loggingEnabled)
    {
        this.loggingConnectionFactory.setLoggingEnabled(loggingEnabled);
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


    private void validateParams(DataSource dataSource, LoggingSqlConfig loggingSqlConfig) throws IllegalArgumentException
    {
        if (dataSource == null) {
            throw new IllegalArgumentException("Must provide a dateSource");
        }
        if (loggingSqlConfig == null) {
            throw new IllegalArgumentException("Must provide a loggingSqlConfig");
        }
    }

}
