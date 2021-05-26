package bwj.logging.jdbc;

import java.sql.Connection;

/**
 * LoggingConnectionCreator makes a LoggingConnection that decorates an existing connection
 */
public class LoggingConnectionCreator
{
    private boolean loggingEnabled = true;  // flag for enabling/disabling
    private final LoggingSqlConfig loggingSqlConfig;

    public LoggingConnectionCreator(LoggingSqlConfig loggingSqlConfig)
    {
        if (loggingSqlConfig == null) {
            throw new IllegalArgumentException("Must provide a loggingSqlConfig");
        }
        this.loggingSqlConfig = loggingSqlConfig;
    }

    /**
     * Creates a LoggingConnection
     * @param connection the original connection to be wrapped with LoggingConnection.
     * @return loggingConnection
     */
    public Connection getConnection(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null.");
        }

        if (this.loggingEnabled) {
            return new LoggingConnection(connection, loggingSqlConfig);
        }
        else {
            return connection;
        }
    }


    /**
     * If true, getConnection will return a "LoggingConnection",
     * If false, getConnection will pass back the original connection given.
     * @return true if sql Logging enabled
     */
    public boolean isLoggingEnabled()
    {
        return loggingEnabled;
    }

    /**
     * Enable/Disable logging
     * @param loggingEnabled loggingEnabled
     */
    public void setLoggingEnabled(boolean loggingEnabled)
    {
        this.loggingEnabled = loggingEnabled;
    }


    public static Builder builder() {
        return new Builder();
    }

    // Builder
    public static class Builder extends LoggingSqlConfig.Builder<Builder> {

        public LoggingConnectionCreator build() {
            LoggingSqlConfig config = super.buildConfig();
            return new LoggingConnectionCreator(config);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }


}
