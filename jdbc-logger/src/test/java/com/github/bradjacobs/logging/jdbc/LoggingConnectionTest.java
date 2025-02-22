package com.github.bradjacobs.logging.jdbc;

import com.github.bradjacobs.logging.jdbc.listeners.LoggingListener;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

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
        assertEquals(2, conn.getLoggingListeners().size(),"mismatch expected log listener count");
    }

    // *** ERROR CONDITION TESTS ***
    /////////////////////////////////

    @Test
    public void testNullLogger() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            LoggingConnection.builder(MOCK_CONNECTION).logger(null).build();
        });
        assertEquals("Must provide a non-null logger.", exception.getMessage());
    }

    @Test
    public void testNullListener() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            LoggingConnection.builder(MOCK_CONNECTION).loggingListener(null).build();
        });
        assertEquals("Must provide at least one logger or loggingListener", exception.getMessage());
    }

    @Test
    public void testNullListenerCollections() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            List<LoggingListener> nullList = null;
            LoggingConnection.builder(MOCK_CONNECTION).loggingListeners(nullList).build();
        });
        assertEquals("Must provide at least one logger or loggingListener", exception.getMessage());
    }

    @Test
    public void testEmptyListenerCollections() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            LoggingConnection.builder(MOCK_CONNECTION).loggingListeners(Collections.emptyList()).build();
        });
        assertEquals("Must provide at least one logger or loggingListener", exception.getMessage());
    }

    @Test
    public void testListWithAllNulls() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            List<LoggingListener> list = Collections.singletonList(null);
            LoggingConnection.builder(MOCK_CONNECTION).loggingListeners(list).build();
        });
        assertEquals("Must provide at least one logger or loggingListener", exception.getMessage());
    }

    @Test
    public void testMissingConnection() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            LoggingConnection.builder(null).logger(logger).build();
        });
        assertEquals(EXPECTED_MISSING_CONNECTION_MSG, exception.getMessage());
    }
}
