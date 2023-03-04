package com.github.bradjacobs.logging.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;

public class LoggingDataSourceTest {
    private static final Logger logger = LoggerFactory.getLogger(LoggingDataSourceTest.class);
    private static final DataSource MOCK_DATA_SOURCE = mock(DataSource.class);

    private static final String MISSING_DATASOURCE_MSG = "Must provide a dateSource";
    private static final String MISSING_LOGGER_MSG = "Must provide a non-null logger.";
    private static final String MISSING_LOG_LISTENER_MSG = "Logging Listeners cannot be set to null or empty collection.";
    private static final String MISSING_BUILDER_MSG = "Must provide a dbLoggingBuilder";

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
            expectedExceptionsMessageRegExp = MISSING_BUILDER_MSG)
    public void testMissingLoggingConnectionCreator() throws Exception {
        DbLoggingBuilder dbLoggingBuilder = null;
        LoggingDataSource loggingDataSource = new LoggingDataSource(MOCK_DATA_SOURCE, dbLoggingBuilder);
    }
}
