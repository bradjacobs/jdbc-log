package com.github.bradjacobs.logging.jdbc.param;

import com.github.bradjacobs.logging.jdbc.DatabaseType;
import org.testng.annotations.Test;

import java.time.ZoneId;
import java.util.Date;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;


public class SqlParamRendererFactoryTest
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


    @Test
    public void testDateStringRendererBaseCase() throws Exception
    {
        ParamRendererFactory rendererFactory = ParamRendererFactory.getDefaultInstance();
        SqlParamRenderer<Date> renderer = rendererFactory.getRenderer(ParamType.TIMESTAMP);
        assertEquals( getRenderedString(renderer, TEST_DATE), EXPECTED_DATETIME_UTC, "mismatch expected date string");
    }

    // no zoneId param will default to UTC
    @Test
    public void testDateStringRendererMissingZone() throws Exception
    {
        ParamRendererFactory rendererFactory = ParamRendererFactory.builder().withZoneId(null).build();
        SqlParamRenderer<Date> renderer = rendererFactory.getRenderer(ParamType.TIMESTAMP);
        assertEquals( getRenderedString(renderer, TEST_DATE), EXPECTED_DATETIME_UTC, "mismatch expected date string");
    }

    // passing in a timezone renders a different date string
    @Test
    public void testDateStringRendererCustomZone() throws Exception
    {
        ParamRendererFactory rendererFactory = ParamRendererFactory.builder().withZoneId(PACITIC_ZONE).build();
        SqlParamRenderer<Date> renderer = rendererFactory.getRenderer(ParamType.TIMESTAMP);
        assertEquals( getRenderedString(renderer, TEST_DATE), EXPECTED_DATETIME_PACIFIC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererSqlTimestamp() throws Exception
    {
        java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(TEST_DATE_LONG);

        ParamRendererFactory rendererFactory = ParamRendererFactory.builder().withZoneId(null).build();
        SqlParamRenderer<Date> renderer = rendererFactory.getRenderer(ParamType.TIMESTAMP);
        assertEquals( getRenderedString(renderer, sqlTimestamp), EXPECTED_DATETIME_UTC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererSqlTimestampWithZone() throws Exception
    {
        java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(TEST_DATE_LONG);

        ParamRendererFactory rendererFactory = ParamRendererFactory.builder().withZoneId(PACITIC_ZONE).build();
        SqlParamRenderer<Date> renderer = rendererFactory.getRenderer(ParamType.TIMESTAMP);
        assertEquals( getRenderedString(renderer, sqlTimestamp), EXPECTED_DATETIME_PACIFIC, "mismatch expected date string");
    }
    
    @Test
    public void testDateStringRendererSqlDate() throws Exception
    {
        java.sql.Date sqlDate = new java.sql.Date(TEST_DATE_LONG);

        ParamRendererFactory rendererFactory = ParamRendererFactory.getDefaultInstance();
        SqlParamRenderer<Date> renderer = rendererFactory.getRenderer(ParamType.DATE);
        assertEquals( getRenderedString(renderer, sqlDate), EXPECTED_DATE_UTC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererSqlDateWithZone() throws Exception
    {
        java.sql.Date sqlDate = new java.sql.Date(TEST_DATE_LONG);

        ParamRendererFactory rendererFactory = ParamRendererFactory.builder().withZoneId(PACITIC_ZONE).build();
        SqlParamRenderer<Date> renderer = rendererFactory.getRenderer(ParamType.DATE);
        assertEquals( getRenderedString(renderer, sqlDate), EXPECTED_DATE_PACIFIC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererSqlTime() throws Exception
    {
        java.sql.Time sqlTime = new java.sql.Time(TEST_DATE_LONG);

        ParamRendererFactory rendererFactory = ParamRendererFactory.getDefaultInstance();
        SqlParamRenderer<Date> renderer = rendererFactory.getRenderer(ParamType.TIME);
        assertEquals( getRenderedString(renderer, sqlTime), EXPECTED_TIME_UTC, "mismatch expected date string");
    }

    @Test
    public void testDateStringRendererSqlTimeWithZone() throws Exception
    {
        java.sql.Time sqlTime = new java.sql.Time(TEST_DATE_LONG);

        ParamRendererFactory rendererFactory = ParamRendererFactory.builder().withZoneId(PACITIC_ZONE).build();
        SqlParamRenderer<Date> renderer = rendererFactory.getRenderer(ParamType.TIME);
        assertEquals( getRenderedString(renderer, sqlTime), EXPECTED_TIME_PACIFIC, "mismatch expected date string");
    }



    // when call the factory with "Oracle", then get back defaults that handles the Oracle date/timestamps differently
    @Test
    public void testOracleDefaults_Timestamp() throws Exception
    {
        java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(TEST_DATE_LONG);

        ParamRendererFactory rendererFactory = ParamRendererFactory.builder().withDatabaseType(DatabaseType.ORACLE).build();
        SqlParamRenderer<Date> renderer = rendererFactory.getRenderer(ParamType.TIMESTAMP);

        String expected = "TO_TIMESTAMP(" + EXPECTED_DATETIME_UTC + ", 'YYYY-MM-DD HH24:MI:SS')";

        String renderedString = getRenderedString(renderer, sqlTimestamp);
        assertEquals(renderedString, expected, "mismatch expected formatted date string");
    }

    // even though this is "Date" the inner part will show date and time
    @Test
    public void testOracleDefaults_Date() throws Exception
    {
        java.sql.Date sqlDate = new java.sql.Date(TEST_DATE_LONG);

        ParamRendererFactory rendererFactory = ParamRendererFactory.builder().withDatabaseType(DatabaseType.ORACLE).build();
        SqlParamRenderer<Date> renderer = rendererFactory.getRenderer(ParamType.DATE);

        String expected = "TO_DATE(" + EXPECTED_DATETIME_UTC +", 'YYYY-MM-DD HH24:MI:SS')";

        String renderedString = getRenderedString(renderer, sqlDate);
        assertEquals(renderedString, expected, "mismatch expected formatted date string");
    }








    private <T> String getRenderedString(SqlParamRenderer<T> renderer, T d) {
        assertNotNull(renderer, "expected non-null renderer object");
        StringBuilder sb = new StringBuilder();
        renderer.appendParamValue(d, sb);
        return sb.toString();
    }
}
