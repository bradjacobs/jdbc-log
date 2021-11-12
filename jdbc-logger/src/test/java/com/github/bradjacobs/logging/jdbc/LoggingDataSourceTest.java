package com.github.bradjacobs.logging.jdbc;

import com.github.bradjacobs.logging.jdbc.listeners.LoggingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.sql.DataSource;

import static org.testng.Assert.*;
import static org.mockito.Mockito.*;

public class LoggingDataSourceTest
{
    private static final Logger logger = LoggerFactory.getLogger(LoggingDataSourceTest.class);
    private static final DataSource MOCK_DATA_SOURCE = mock(DataSource.class);

    private static final String MISSING_DATASOURCE_MSG = "Must provide a dateSource";
    private static final String MISSING_LOGGER_MSG = "Must provide a non-null logger.";
    private static final String MISSING_LOG_LISTENER_MSG = "Logging Listeners cannot be set to null or empty collection.";
    private static final String MISSING_LOG_CONN_CREATOR_MSG = "Must provide a loggingConnectionCreator";



    // exception handling unittests ....

    @Test(expectedExceptions = { IllegalArgumentException.class },
            expectedExceptionsMessageRegExp = MISSING_DATASOURCE_MSG)
    public void testMissingDataSource() throws Exception {

        LoggingDataSource loggingDataSource = new LoggingDataSource(null, logger);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
            expectedExceptionsMessageRegExp = MISSING_LOGGER_MSG)
    public void testMissingLogger() throws Exception {

        Logger nullLogger = null;
        LoggingDataSource loggingDataSource = new LoggingDataSource(MOCK_DATA_SOURCE, nullLogger);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
            expectedExceptionsMessageRegExp = MISSING_LOG_LISTENER_MSG)
    public void testMissingLoggingListenersA() throws Exception {

        LoggingListener[] nullLoggingListeners = null;
        LoggingDataSource loggingDataSource = new LoggingDataSource(MOCK_DATA_SOURCE, nullLoggingListeners);
    }
    @Test(expectedExceptions = { IllegalArgumentException.class },
            expectedExceptionsMessageRegExp = MISSING_LOG_LISTENER_MSG)
    public void testMissingLoggingListenersB() throws Exception {

        LoggingListener[] nullLoggingListeners = new LoggingListener[0];
        LoggingDataSource loggingDataSource = new LoggingDataSource(MOCK_DATA_SOURCE, nullLoggingListeners);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
            expectedExceptionsMessageRegExp = MISSING_LOG_CONN_CREATOR_MSG)
    public void testMissingLoggingConnectionCreator() throws Exception {

        LoggingConnectionCreator loggingConnectionCreator = null;
        LoggingDataSource loggingDataSource = new LoggingDataSource(MOCK_DATA_SOURCE, loggingConnectionCreator);
    }
}
