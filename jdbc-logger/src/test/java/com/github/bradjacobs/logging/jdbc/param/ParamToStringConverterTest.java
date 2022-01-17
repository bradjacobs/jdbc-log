package com.github.bradjacobs.logging.jdbc.param;

import com.github.bradjacobs.logging.jdbc.DatabaseType;
import com.github.bradjacobs.logging.jdbc.param.ParamToStringConverter;
import org.testng.annotations.Test;

import java.time.ZoneId;
import java.util.Date;

import static org.testng.Assert.assertEquals;

public class ParamToStringConverterTest
{
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final ZoneId PACITIC_ZONE = ZoneId.of("US/Pacific");

    private static final long TEST_DATE_LONG = 1538014031000L;
    private static final Date TEST_DATE = new Date(TEST_DATE_LONG);
    private static final String EXPECTED_DATETIME_UTC = "'2018-09-27 02:07:11'";
    private static final String EXPECTED_DATETIME_PACIFIC = "'2018-09-26 19:07:11'";
    private static final String EXPECTED_DATE_UTC = "'2018-09-27'";
    private static final String EXPECTED_DATE_PACIFIC = "'2018-09-26'";
    private static final String EXPECTED_TIME_UTC = "'02:07:11'";
    private static final String EXPECTED_TIME_PACIFIC = "'19:07:11'";

    private ParamToStringConverter converter = new ParamToStringConverter();

    @Test
    public void testTrue() throws Exception
    {
        Boolean input = true;
        String expectedString = "1";
        assertEquals( converter.convertToString(input), expectedString, "mismatch expected string");
    }

    @Test
    public void testFalse() throws Exception
    {
        Boolean input = false;
        String expectedString = "0";
        assertEquals( converter.convertToString(input), expectedString, "mismatch expected string");
    }

    @Test
    public void testQuoteString() throws Exception
    {
        String inputString = "my test string";
        String expectedString = "'" + inputString + "'";
        assertEquals( converter.convertToString(inputString), expectedString, "mismatch expected string");
    }

    @Test
    public void testEscapedQuoteString() throws Exception
    {
        String inputString = "Can't buy me love";
        String expectedString = "'" + inputString.replace("'", "''") + "'";
        assertEquals( converter.convertToString(inputString), expectedString, "mismatch expected string");
    }

    @Test
    public void testSimpleNumber() throws Exception
    {
        Long inputValue = 931L;
        String expectedString = String.valueOf(inputValue);
        assertEquals( converter.convertToString(inputValue), expectedString, "mismatch expected number string");
    }

    /**
     * Test that double does _NOT_ show up like 1.2E-7
     */
    @Test
    public void testNoScientificNotation() throws Exception
    {
        Double inputValue = 0.00000012d;
        String expectedString = "0.00000012";
        assertEquals( converter.convertToString(inputValue), expectedString, "mismatch expected number string");
    }



    ///// Date tests below...

    @Test
    public void testDateStringRendererBaseCase() throws Exception
    {
        assertEquals( converter.convertToString(TEST_DATE), EXPECTED_DATETIME_UTC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererWithUTC() throws Exception
    {
        ParamToStringConverter utcConverter = new ParamToStringConverter(UTC_ZONE);
        assertEquals( utcConverter.convertToString(TEST_DATE), EXPECTED_DATETIME_UTC, "mismatch expected date string");
    }

    // passing in a timezone renders a different date string
    @Test
    public void testDateStringRendererCustomZone() throws Exception
    {
        ParamToStringConverter pacificConverter = new ParamToStringConverter(PACITIC_ZONE);
        assertEquals( pacificConverter.convertToString(TEST_DATE), EXPECTED_DATETIME_PACIFIC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererSqlTimestamp() throws Exception
    {
        java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(TEST_DATE_LONG);
        assertEquals( converter.convertToString(sqlTimestamp), EXPECTED_DATETIME_UTC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererSqlTimestampWithZone() throws Exception
    {
        java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(TEST_DATE_LONG);
        ParamToStringConverter pacificConverter = new ParamToStringConverter(PACITIC_ZONE);
        assertEquals( pacificConverter.convertToString(sqlTimestamp), EXPECTED_DATETIME_PACIFIC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererSqlDate() throws Exception
    {
        java.sql.Date sqlDate = new java.sql.Date(TEST_DATE_LONG);
        assertEquals( converter.convertToString(sqlDate), EXPECTED_DATE_UTC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererSqlDateWithZone() throws Exception
    {
        java.sql.Date sqlDate = new java.sql.Date(TEST_DATE_LONG);
        ParamToStringConverter pacificConverter = new ParamToStringConverter(PACITIC_ZONE);
        assertEquals( pacificConverter.convertToString(sqlDate), EXPECTED_DATE_PACIFIC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererSqlTime() throws Exception
    {
        java.sql.Time sqlTime = new java.sql.Time(TEST_DATE_LONG);
        assertEquals( converter.convertToString(sqlTime), EXPECTED_TIME_UTC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererSqlTimeWithZone() throws Exception
    {
        java.sql.Time sqlTime = new java.sql.Time(TEST_DATE_LONG);
        ParamToStringConverter pacificConverter = new ParamToStringConverter(PACITIC_ZONE);
        assertEquals( pacificConverter.convertToString(sqlTime), EXPECTED_TIME_PACIFIC, "mismatch expected date string");
    }


    @Test
    public void testDateNumeric() throws Exception
    {
        java.sql.Time sqlTime = new java.sql.Time(TEST_DATE_LONG);
        String expectedString = String.valueOf(TEST_DATE_LONG);

        ParamToStringConverter numericDateConverter = new ParamToStringConverter();
        numericDateConverter.setRenderDatesAsEpochUtc(true);
        assertEquals( numericDateConverter.convertToString(sqlTime), expectedString, "mismatch expected date string");
    }



    // Special cases for Oracle Dates


    // when call the factory with "Oracle", then get back defaults that handles the Oracle date/timestamps differently
    @Test
    public void testOracleDefaults_Timestamp() throws Exception
    {
        java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(TEST_DATE_LONG);

        ParamToStringConverter oracleDateConverter = new ParamToStringConverter();
        oracleDateConverter.setDatabaseType(DatabaseType.ORACLE);

        String expected = "TO_TIMESTAMP(" + EXPECTED_DATETIME_UTC + ", 'YYYY-MM-DD HH24:MI:SS')";

        String renderedString = oracleDateConverter.convertToString(sqlTimestamp);
        assertEquals(renderedString, expected, "mismatch expected formatted date string");
    }

    // even though this is "Date" the inner part will show date and time
    @Test
    public void testOracleDefaults_Date() throws Exception
    {
        java.sql.Date sqlDate = new java.sql.Date(TEST_DATE_LONG);

        ParamToStringConverter oracleDateConverter = new ParamToStringConverter();
        oracleDateConverter.setDatabaseType(DatabaseType.ORACLE);

        String expected = "TO_DATE(" + EXPECTED_DATE_UTC +", 'YYYY-MM-DD')";

        String renderedString = oracleDateConverter.convertToString(sqlDate);
        assertEquals(renderedString, expected, "mismatch expected formatted date string");
    }


}
