package bwj.logging.jdbc;

import java.sql.Connection;
import java.time.ZoneId;

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
     * @param targetConnection the original connection to be wrapped/decorated with LoggingConnection.
     * @return loggingConnection
     */
    public Connection getConnection(Connection targetConnection) {
        if (targetConnection == null) {
            throw new IllegalArgumentException("Connection cannot be null.");
        }

        if (this.loggingEnabled) {
            return new LoggingConnection(targetConnection, loggingSqlConfig);
        }
        else {
            return targetConnection;
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
        return new Builder(null);
    }
    public static Builder builder(ZoneId zoneId) {
        return new Builder(zoneId);
    }

    // Builder
    public static class Builder extends LoggingSqlConfig.Builder<Builder> {

        protected Builder(ZoneId zoneId) { super(zoneId); }

        public LoggingConnectionCreator build() {
            LoggingSqlConfig config = super.buildConfig();
            return new LoggingConnectionCreator(config);
        }

        @Override
        public Builder getThis() {
            return this;
        }
    }


}
