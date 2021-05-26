package bwj.logging.jdbc;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * TODO - must rename this class   'factory' is misleading
 * LoggingConnectionFactory creates a LoggingConnection that decorates an existing connection
 */
public class LoggingConnectionFactory
{
    private final LoggingSqlConfig loggingSqlConfig;
    private boolean loggingEnabled = true;

    public LoggingConnectionFactory(LoggingSqlConfig loggingSqlConfig)
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
     * If true, build will return a "LoggingConnection",
     * If falsae, build will pass back the original connection given.
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

}
