package com.github.bradjacobs.logging.jdbc.param;

import org.testng.annotations.Test;

import java.time.ZoneId;
import java.util.Date;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;


public class SqlParamRendererGeneratorTest
{
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final ZoneId PACITIC_ZONE = ZoneId.of("US/Pacific");


    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";

    private static final long TEST_DATE_LONG = 1538014031000L;
    private static final Date TEST_DATE = new Date(TEST_DATE_LONG);
    private static final String EXPECTED_DATETIME_UTC = "'2018-09-27 02:07:11'";
    private static final String EXPECTED_DATETIME_PACIFIC = "'2018-09-26 19:07:11'";
    private static final String EXPECTED_DATE_UTC = "'2018-09-27'";
    private static final String EXPECTED_DATE_PACIFIC = "'2018-09-26'";
    private static final String EXPECTED_TIME_UTC = "'02:07:11'";
    private static final String EXPECTED_TIME_PACIFIC = "'19:07:11'";


    private SqlParamRendererGenerator paramRendererGenerator = new SqlParamRendererGenerator();

    @Test
    public void testDateStringRendererBaseCase() throws Exception
    {
        SqlParamRenderer<Date> renderer = paramRendererGenerator.createDateStringParamRenderer(TIMESTAMP_PATTERN, UTC_ZONE);
        assertEquals( getRenderedString(renderer, TEST_DATE), EXPECTED_DATETIME_UTC, "mismatch expected date string");
    }

    // no zoneId param will default to UTC
    @Test
    public void testDateStringRendererMissingZone() throws Exception
    {
        SqlParamRenderer<Date> renderer = paramRendererGenerator.createDateStringParamRenderer(TIMESTAMP_PATTERN, null);
        assertEquals( getRenderedString(renderer, TEST_DATE), EXPECTED_DATETIME_UTC, "mismatch expected date string");
    }

    // passing in a timezone renders a different date string
    @Test
    public void testDateStringRendererCustomZone() throws Exception
    {
        SqlParamRenderer<Date> renderer = paramRendererGenerator.createDateStringParamRenderer(TIMESTAMP_PATTERN, PACITIC_ZONE);
        assertEquals( getRenderedString(renderer, TEST_DATE), EXPECTED_DATETIME_PACIFIC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererSqlTimestamp() throws Exception
    {
        java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(TEST_DATE_LONG);
        SqlParamRenderer<Date> renderer = paramRendererGenerator.createDateStringParamRenderer(TIMESTAMP_PATTERN, UTC_ZONE);
        assertEquals( getRenderedString(renderer, sqlTimestamp), EXPECTED_DATETIME_UTC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererSqlTimestampWithZone() throws Exception
    {
        java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(TEST_DATE_LONG);
        SqlParamRenderer<Date> renderer = paramRendererGenerator.createDateStringParamRenderer(TIMESTAMP_PATTERN, PACITIC_ZONE);
        assertEquals( getRenderedString(renderer, sqlTimestamp), EXPECTED_DATETIME_PACIFIC, "mismatch expected date string");
    }
    
    @Test
    public void testDateStringRendererSqlDate() throws Exception
    {
        java.sql.Date sqlDate = new java.sql.Date(TEST_DATE_LONG);
        SqlParamRenderer<Date> renderer = paramRendererGenerator.createDateStringParamRenderer(DATE_PATTERN, UTC_ZONE);
        assertEquals( getRenderedString(renderer, sqlDate), EXPECTED_DATE_UTC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererSqlDateWithZone() throws Exception
    {
        java.sql.Date sqlDate = new java.sql.Date(TEST_DATE_LONG);
        SqlParamRenderer<Date> renderer = paramRendererGenerator.createDateStringParamRenderer(DATE_PATTERN, PACITIC_ZONE);
        assertEquals( getRenderedString(renderer, sqlDate), EXPECTED_DATE_PACIFIC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererSqlTime() throws Exception
    {
        java.sql.Time sqlTime = new java.sql.Time(TEST_DATE_LONG);
        SqlParamRenderer<Date> renderer = paramRendererGenerator.createDateStringParamRenderer(TIME_PATTERN, UTC_ZONE);
        assertEquals( getRenderedString(renderer, sqlTime), EXPECTED_TIME_UTC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererSqlTimeWithZone() throws Exception
    {
        java.sql.Time sqlTime = new java.sql.Time(TEST_DATE_LONG);
        SqlParamRenderer<Date> renderer = paramRendererGenerator.createDateStringParamRenderer(TIME_PATTERN, PACITIC_ZONE);
        assertEquals( getRenderedString(renderer, sqlTime), EXPECTED_TIME_PACIFIC, "mismatch expected date string");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
        expectedExceptionsMessageRegExp = "datetime formatter pattern is required.")
    public void testDateStringRenderer_MissingPattern_A() throws Exception
    {
        paramRendererGenerator.createDateStringParamRenderer("", UTC_ZONE);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
        expectedExceptionsMessageRegExp = "datetime formatter pattern is required.")
    public void testDateStringRenderer_MissingPattern_B() throws Exception
    {
        paramRendererGenerator.createDateStringParamRenderer(null, UTC_ZONE);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
        expectedExceptionsMessageRegExp = "Invalid DateFormat pattern.*")
    public void testDateStringRenderer_InvalidPattern() throws Exception
    {
        paramRendererGenerator.createDateStringParamRenderer("yyyy_BOGUS_", UTC_ZONE);
    }


    @Test
    public void testDateNumeric() throws Exception
    {
        java.sql.Time sqlTime = new java.sql.Time(TEST_DATE_LONG);
        String expectedString = String.valueOf(TEST_DATE_LONG);
        SqlParamRenderer<Date> renderer = paramRendererGenerator.createDateNumericParamRenderer();
        assertEquals( getRenderedString(renderer, sqlTime), expectedString, "mismatch expected date string");
    }


    @Test
    public void testPrefixSuffix() throws Exception
    {
        String prefix = "PRE_";
        String suffix = "_POST";
        Integer value = 77;
        String expected = prefix + value + suffix;

        SqlParamRenderer<Object> basicRenderer = paramRendererGenerator.createBasicParamRenderer();
        SqlParamRenderer<Object> prefixSuffixRenderer = paramRendererGenerator.createPrefixSufixParamRenderer(basicRenderer, prefix, suffix);

        String renderedString = getRenderedString(prefixSuffixRenderer, value);
        assertEquals( renderedString, expected, "mismatch expected prefix/suffix string");
    }

    @Test
    public void testPrefixSuffix_NoPrefix() throws Exception
    {
        String prefix = "";
        String suffix = "_POST";
        Integer value = 77;
        String expected = prefix + value + suffix;

        SqlParamRenderer<Object> basicRenderer = paramRendererGenerator.createBasicParamRenderer();
        SqlParamRenderer<Object> prefixSuffixRenderer = paramRendererGenerator.createPrefixSufixParamRenderer(basicRenderer, prefix, suffix);

        String renderedString = getRenderedString(prefixSuffixRenderer, value);
        assertEquals( renderedString, expected, "mismatch expected prefix/suffix string");
    }

    @Test
    public void testPrefixSuffix_NoSuffix() throws Exception
    {
        String prefix = "PRE_";
        String suffix = null;
        Integer value = 77;
        String expected = prefix + value;

        SqlParamRenderer<Object> basicRenderer = paramRendererGenerator.createBasicParamRenderer();
        SqlParamRenderer<Object> prefixSuffixRenderer = paramRendererGenerator.createPrefixSufixParamRenderer(basicRenderer, prefix, suffix);

        String renderedString = getRenderedString(prefixSuffixRenderer, value);
        assertEquals( renderedString, expected, "mismatch expected prefix/suffix string");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
        expectedExceptionsMessageRegExp = "Renderer parameter is required.")
    public void testPrefixSuffix_BadInput() throws Exception
    {
        paramRendererGenerator.createPrefixSufixParamRenderer(null, "PREFIX", "SUFFIX");
    }



    private <T> String getRenderedString(SqlParamRenderer<T> renderer, T d) {
        assertNotNull(renderer, "expected non-null renderer object");
        StringBuilder sb = new StringBuilder();
        renderer.appendParamValue(d, sb);
        return sb.toString();
    }


}
