package com.github.bradjacobs.logging.jdbc.param;

import org.testng.annotations.Test;

import java.time.ZoneId;
import java.util.Date;

import static org.testng.Assert.assertEquals;

public class DefaultParamToStringConverterTest {
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final ZoneId PACIFIC_ZONE = ZoneId.of("US/Pacific");

    private static final long TEST_DATE_LONG = 1538014031000L;
    private static final Date TEST_DATE = new Date(TEST_DATE_LONG);
    private static final String EXPECTED_DATETIME_UTC = "'2018-09-27 02:07:11'";
    private static final String EXPECTED_DATETIME_PACIFIC = "'2018-09-26 19:07:11'";
    private static final String EXPECTED_DATE_UTC = "'2018-09-27'";
    private static final String EXPECTED_DATE_PACIFIC = "'2018-09-26'";
    private static final String EXPECTED_TIME_UTC = "'02:07:11'";
    private static final String EXPECTED_TIME_PACIFIC = "'19:07:11'";

    private final ParamToStringConverter converter = new DefaultParamToStringConverter();

    @Test
    public void testTrue() {
        assertEquals( converter.convertToString(true), "1", "mismatch expected string");
    }

    @Test
    public void testFalse() {
        assertEquals( converter.convertToString(false), "0", "mismatch expected string");
    }

    @Test
    public void testQuoteString() {
        String inputString = "my test string";
        String expectedString = "'" + inputString + "'";
        assertEquals( converter.convertToString(inputString), expectedString, "mismatch expected string");
    }

    @Test
    public void testEscapedQuoteString() {
        String inputString = "Can't buy me love";
        String expectedString = "'" + inputString.replace("'", "''") + "'";
        assertEquals( converter.convertToString(inputString), expectedString, "mismatch expected string");
    }

    @Test
    public void testSimpleNumber() {
        Long inputValue = 931L;
        String expectedString = String.valueOf(inputValue);
        assertEquals( converter.convertToString(inputValue), expectedString, "mismatch expected number string");
    }

    /**
     * Test that double does _NOT_ show up like 1.2E-7
     */
    @Test
    public void testNoScientificNotation() {
        Double inputValue = 0.00000012d;
        String expectedString = "0.00000012";
        assertEquals( converter.convertToString(inputValue), expectedString, "mismatch expected number string");
    }

    ///// Date tests below...
    @Test
    public void testDateStringRendererBaseCase() {
        assertEquals( converter.convertToString(TEST_DATE), EXPECTED_DATETIME_UTC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererWithUTC() {
        ParamToStringConverter utcConverter = new DefaultParamToStringConverter(UTC_ZONE);
        assertEquals( utcConverter.convertToString(TEST_DATE), EXPECTED_DATETIME_UTC, "mismatch expected date string");
    }

    // passing in a timezone renders a different date string
    @Test
    public void testDateStringRendererCustomZone() {
        ParamToStringConverter pacificConverter = new DefaultParamToStringConverter(PACIFIC_ZONE);
        assertEquals( pacificConverter.convertToString(TEST_DATE), EXPECTED_DATETIME_PACIFIC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererSqlTimestamp() {
        java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(TEST_DATE_LONG);
        assertEquals( converter.convertToString(sqlTimestamp), EXPECTED_DATETIME_UTC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererSqlTimestampWithZone() {
        java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(TEST_DATE_LONG);
        ParamToStringConverter pacificConverter = new DefaultParamToStringConverter(PACIFIC_ZONE);
        assertEquals( pacificConverter.convertToString(sqlTimestamp), EXPECTED_DATETIME_PACIFIC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererSqlDate() {
        java.sql.Date sqlDate = new java.sql.Date(TEST_DATE_LONG);
        assertEquals( converter.convertToString(sqlDate), EXPECTED_DATE_UTC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererSqlDateWithZone() {
        java.sql.Date sqlDate = new java.sql.Date(TEST_DATE_LONG);
        ParamToStringConverter pacificConverter = new DefaultParamToStringConverter(PACIFIC_ZONE);
        assertEquals( pacificConverter.convertToString(sqlDate), EXPECTED_DATE_PACIFIC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererSqlTime() {
        java.sql.Time sqlTime = new java.sql.Time(TEST_DATE_LONG);
        assertEquals( converter.convertToString(sqlTime), EXPECTED_TIME_UTC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererSqlTimeWithZone() {
        java.sql.Time sqlTime = new java.sql.Time(TEST_DATE_LONG);
        ParamToStringConverter pacificConverter = new DefaultParamToStringConverter(PACIFIC_ZONE);
        assertEquals( pacificConverter.convertToString(sqlTime), EXPECTED_TIME_PACIFIC, "mismatch expected date string");
    }
}
