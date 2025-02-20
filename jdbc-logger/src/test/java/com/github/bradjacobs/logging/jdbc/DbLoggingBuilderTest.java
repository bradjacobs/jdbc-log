package com.github.bradjacobs.logging.jdbc;

import com.github.bradjacobs.logging.jdbc.listeners.LoggingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class DbLoggingBuilderTest {
    // todo - figure out what these were for or remove them.
    private static final String EXPECTED_MISSING_PATTERN_MSG = "datetime formatter pattern is required.";
    private static final String EXPECTED_INVALID_PATTERN_SUBSTRING_MSG = "Invalid DateFormat pattern:.*";
    private static final String EXPECTED_MISSING_CONNECTION_MSG = "Connection cannot be null.";

    private static final DataSource MOCK_DATA_SOURCE = mock(DataSource.class);
    private static final Logger logger = LoggerFactory.getLogger(DbLoggingBuilderTest.class);

    @Test
    public void testSimple() {
        DbLoggingBuilder.builder(logger);
    }

    @Test
    public void testSetLoggingListeners() {
        LoggingListener dummyLogger1 = sql -> { };
        LoggingListener dummyLogger2 = sql -> { };

        DbLoggingBuilder dbLoggingBuilder =
            DbLoggingBuilder.builder(dummyLogger1, dummyLogger2);

        assertNotNull(dbLoggingBuilder.loggingListeners);
        assertEquals(dbLoggingBuilder.loggingListeners.size(), 2, "mismatch expected log listener count");
    }

    // *** ERROR CONDITION TESTS ***
    /////////////////////////////////

    @Test(expectedExceptions = { IllegalArgumentException.class },
        expectedExceptionsMessageRegExp = "Must provide a non-null logger.")
    public void testNullLogger() {
        org.slf4j.Logger logger = null;
        DbLoggingBuilder.builder(logger).createFrom(MOCK_DATA_SOURCE);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
        expectedExceptionsMessageRegExp = "Must provide at least one logger or loggingListener")
    public void testNullListener() {
        LoggingListener logListener = null;
        DbLoggingBuilder.builder(logListener).createFrom(MOCK_DATA_SOURCE);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
            expectedExceptionsMessageRegExp = "Must provide at least one logger or loggingListener")
    public void testNullListenerCollections() {
        List<LoggingListener> nullList = null;
        DbLoggingBuilder.builder(nullList).createFrom(MOCK_DATA_SOURCE);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
            expectedExceptionsMessageRegExp = "Must provide at least one logger or loggingListener")
    public void testEmptyListenerCollections() {
        DbLoggingBuilder.builder(Collections.emptyList()).createFrom(MOCK_DATA_SOURCE);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
            expectedExceptionsMessageRegExp = "Must provide at least one logger or loggingListener")
    public void testListWithAllNulls() {
        List<LoggingListener> list = Collections.singletonList(null);
        DbLoggingBuilder.builder(list).createFrom(MOCK_DATA_SOURCE);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
            expectedExceptionsMessageRegExp = EXPECTED_MISSING_CONNECTION_MSG)
    public void testMissingConnection() {
        Connection nullConnection = null;
        DbLoggingBuilder.builder(logger).createFrom(nullConnection);
    }
}
