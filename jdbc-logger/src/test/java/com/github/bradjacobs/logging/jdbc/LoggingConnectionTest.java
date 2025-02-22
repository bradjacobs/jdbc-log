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

public class LoggingConnectionTest {
    // todo - figure out what these were for or remove them.
    private static final String EXPECTED_MISSING_PATTERN_MSG = "datetime formatter pattern is required.";
    private static final String EXPECTED_INVALID_PATTERN_SUBSTRING_MSG = "Invalid DateFormat pattern:.*";
    private static final String EXPECTED_MISSING_CONNECTION_MSG = "Must provide a target connection.";

    private static final DataSource MOCK_DATA_SOURCE = mock(DataSource.class);
    private static final Connection MOCK_CONNECTION = mock(Connection.class);
    private static final Logger logger = LoggerFactory.getLogger(LoggingConnectionTest.class);

    @Test
    public void testSimple() {
        LoggingConnection.builder(MOCK_CONNECTION).logger(logger).build();
    }

    @Test
    public void testSetLoggingListeners() {
        LoggingListener dummyLogger1 = sql -> { };
        LoggingListener dummyLogger2 = sql -> { };

        LoggingConnection conn = LoggingConnection.builder(MOCK_CONNECTION)
                .loggingListeners(dummyLogger1, dummyLogger2).build();
        assertEquals(conn.getLoggingListeners().size(), 2, "mismatch expected log listener count");
    }

    // *** ERROR CONDITION TESTS ***
    /////////////////////////////////

    @Test(expectedExceptions = { IllegalArgumentException.class },
        expectedExceptionsMessageRegExp = "Must provide a non-null logger.")
    public void testNullLogger() {
        LoggingConnection.builder(MOCK_CONNECTION).logger(null).build();
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
        expectedExceptionsMessageRegExp = "Must provide at least one logger or loggingListener")
    public void testNullListener() {
        LoggingConnection.builder(MOCK_CONNECTION).loggingListener(null).build();
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
            expectedExceptionsMessageRegExp = "Must provide at least one logger or loggingListener")
    public void testNullListenerCollections() {
        List<LoggingListener> nullList = null;
        LoggingConnection.builder(MOCK_CONNECTION).loggingListeners(nullList).build();
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
            expectedExceptionsMessageRegExp = "Must provide at least one logger or loggingListener")
    public void testEmptyListenerCollections() {
        LoggingConnection.builder(MOCK_CONNECTION).loggingListeners(Collections.emptyList()).build();
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
            expectedExceptionsMessageRegExp = "Must provide at least one logger or loggingListener")
    public void testListWithAllNulls() {
        List<LoggingListener> list = Collections.singletonList(null);
        LoggingConnection.builder(MOCK_CONNECTION).loggingListeners(list).build();
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
            expectedExceptionsMessageRegExp = EXPECTED_MISSING_CONNECTION_MSG)
    public void testMissingConnection() {
        LoggingConnection.builder(null).logger(logger).build();
    }
}
