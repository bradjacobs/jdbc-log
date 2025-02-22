package com.github.bradjacobs.logging.jdbc;


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
public class LoggingDataSource implements DataSource {
    private final DataSource targetDataSource;
    // hold onto a loggingConnectionBuilder because it will be used to make multiple logging connections.
    private final LoggingConnection.Builder loggingConnectionBuilder;
    private boolean enabled = true;

    public static Builder builder(DataSource targetDataSource) {
        return new Builder(targetDataSource);
    }

    public static class Builder extends AbstractLoggingBuilder<Builder> {
        private DataSource targetDataSource;
        private Builder(DataSource targetDataSource) {
            this.targetDataSource = targetDataSource;
        }

        public Builder targetDataSource(DataSource targetDataSource) {
            this.targetDataSource = targetDataSource;
            return this;
        }

        public LoggingDataSource build() {
            LoggingConnection.Builder loggingConnectionBuilder =
                    LoggingConnection.builder(null)
                            .clobParamLogging(this.clobParamLogging)
                            .zoneId(this.zoneId)
                            .dbType(this.dbType)
                            .loggingListeners(this.loggingListeners);
            return new LoggingDataSource(targetDataSource, loggingConnectionBuilder);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }



    /**
     * Constructor to use any customized loggingConnectionCreator.
     * @param targetDataSource targetDataSource
     * @param loggingConnectionBuilder loggingConnectionCreator
     */
    public LoggingDataSource(DataSource targetDataSource, LoggingConnection.Builder loggingConnectionBuilder) {
        validateParams(targetDataSource, loggingConnectionBuilder);
        this.targetDataSource = targetDataSource;
        this.loggingConnectionBuilder = loggingConnectionBuilder;
    }

    /** @inheritDoc */
    @Override
    public Connection getConnection() throws SQLException {
        Connection innerConnection = targetDataSource.getConnection();
        return createConnection(innerConnection);
    }

    /** @inheritDoc */
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection innerConnection = targetDataSource.getConnection(username, password);
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
        loggingConnectionBuilder.targetConnection(innerConnection);
        return loggingConnectionBuilder.build();
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
    public PrintWriter getLogWriter() throws SQLException {
        return targetDataSource.getLogWriter();
    }

    /** @inheritDoc */
    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        targetDataSource.setLogWriter(out);
    }

    /** @inheritDoc */
    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        targetDataSource.setLoginTimeout(seconds);
    }

    /** @inheritDoc */
    @Override
    public int getLoginTimeout() throws SQLException {
        return targetDataSource.getLoginTimeout();
    }

    /** @inheritDoc */
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return targetDataSource.getParentLogger();
    }
    
    /** @inheritDoc */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return targetDataSource.unwrap(iface);
    }

    /** @inheritDoc */
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return targetDataSource.isWrapperFor(iface);
    }

    private void validateParams(DataSource dataSource, LoggingConnection.Builder loggingConnectionBuilder)
            throws IllegalArgumentException
    {
        if (dataSource == null) {
            throw new IllegalArgumentException("Must provide a dateSource");
        }
        if (loggingConnectionBuilder.loggingListeners.isEmpty()) {
            throw new IllegalArgumentException("Must provide at least one loggingListener.");
        }
    }
}
