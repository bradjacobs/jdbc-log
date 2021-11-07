package com.github.bradjacobs.logging.jdbc;

import com.github.bradjacobs.logging.jdbc.listeners.LoggingListener;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static org.testng.Assert.*;


public class LoggingConnectionCreatorTest
{
    private static final String EXPECTED_MISSING_PATTERN_MSG = "datetime formatter pattern is required.";
    private static final String EXPECTED_INVALID_PATTERN_SUBSTRING_MSG = "Invalid DateFormat pattern:.*";
    private static final String EXPECTED_MISSING_CONNECTION_MSG = "Connection cannot be null.";

    // todo - more tests still needed  dbtype, enable/disable...etc

    @Test
    public void testSimple() throws Exception
    {
        LoggingConnectionCreator.builder().build();
    }

    @Test
    public void testSetLoggingListeners() throws Exception
    {
        LoggingListener logger1 = new LoggingListener() {
            @Override
            public void log(String sql) { }
        };
        LoggingListener logger2 = new LoggingListener() {
            @Override
            public void log(String sql) { }
        };

        LoggingConnectionCreator loggingConnectionCreator =
            LoggingConnectionCreator.builder()
                .withLogListener(logger1, logger2)
                .build();

        assertNotNull(loggingConnectionCreator);
        assertNotNull(loggingConnectionCreator.getLoggingListeners());
        assertEquals(loggingConnectionCreator.getLoggingListeners().size(), 2, "mismatch expected log listener count");
    }


    // technically 'legal', but won't actually affect anything
    @Test
    public void testNullChronoParamRenderer() throws Exception {
        LoggingConnectionCreator.builder().withChronoParamRenderer(null).build();
    }


    // *** ERROR CONDITION TESTS ***
    /////////////////////////////////

    @Test(expectedExceptions = { IllegalArgumentException.class },
        expectedExceptionsMessageRegExp = "Logging Listeners cannot be set to null or empty collection.")
    public void testNullListener() throws Exception
    {
        LoggingListener logListener = null;
        LoggingConnectionCreator.builder().withLogListener(logListener).build();
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
        expectedExceptionsMessageRegExp = "Logging Listeners cannot be set to null or empty collection.")
    public void testNullListener2() throws Exception
    {
        LoggingListener logListener = null;
        List<LoggingListener> logListenerList = Collections.singletonList(logListener);
        LoggingConnectionCreator.builder().withLogListener(logListenerList).build();
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
        expectedExceptionsMessageRegExp = EXPECTED_MISSING_PATTERN_MSG)
    public void testNullPatternTimeStamp() throws Exception {
        LoggingConnectionCreator.builder().withTimestampOnlyCustomPattern(null).build();
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
        expectedExceptionsMessageRegExp = EXPECTED_MISSING_PATTERN_MSG)
    public void testNullPatternDate() throws Exception {
        LoggingConnectionCreator.builder().withDateOnlyCustomPattern(null).build();
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
        expectedExceptionsMessageRegExp = EXPECTED_MISSING_PATTERN_MSG)
    public void testNullPatternTime() throws Exception {
        LoggingConnectionCreator.builder().withTimeOnlyCustomPattern(null).build();
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
        expectedExceptionsMessageRegExp = EXPECTED_INVALID_PATTERN_SUBSTRING_MSG)
    public void testInvalidPatternTimeStamp() throws Exception {
        LoggingConnectionCreator.builder().withTimestampOnlyCustomPattern("YYYY_INVALID_pattern").build();
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
        expectedExceptionsMessageRegExp = EXPECTED_INVALID_PATTERN_SUBSTRING_MSG)
    public void testInvalidPatternDate() throws Exception {
        LoggingConnectionCreator.builder().withDateOnlyCustomPattern("YYYY_INVALID_pattern").build();
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
        expectedExceptionsMessageRegExp = EXPECTED_INVALID_PATTERN_SUBSTRING_MSG)
    public void testInvalidPatternTime() throws Exception {
        LoggingConnectionCreator.builder().withTimeOnlyCustomPattern("YYYY_INVALID_pattern").build();
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
            expectedExceptionsMessageRegExp = EXPECTED_MISSING_CONNECTION_MSG)
    public void testMissingConnection() throws Exception {
        LoggingConnectionCreator loggingConnectionCreator = LoggingConnectionCreator.builder().build();
        loggingConnectionCreator.create(null);
    }
}
