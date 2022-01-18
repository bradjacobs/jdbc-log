package com.github.bradjacobs.logging.jdbc.param;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class OracleParamToStringConverterTest
{
    private static final long TEST_DATE_LONG = 1538014031000L;
    private static final String EXPECTED_DATETIME_UTC = "'2018-09-27 02:07:11'";
    private static final String EXPECTED_DATE_UTC = "'2018-09-27'";


    // when call the factory with "Oracle", then get back defaults that handles the Oracle date/timestamps differently
    @Test
    public void testOracleDefaults_Timestamp() throws Exception
    {
        java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(TEST_DATE_LONG);
        ParamToStringConverter oracleDateConverter = new OracleParamToStringConverter();

        String expected = "TO_TIMESTAMP(" + EXPECTED_DATETIME_UTC + ", 'YYYY-MM-DD HH24:MI:SS')";

        String renderedString = oracleDateConverter.convertToString(sqlTimestamp);
        assertEquals(renderedString, expected, "mismatch expected formatted date string");
    }

    // even though this is "Date" the inner part will show date and time
    @Test
    public void testOracleDefaults_Date() throws Exception
    {
        java.sql.Date sqlDate = new java.sql.Date(TEST_DATE_LONG);
        ParamToStringConverter oracleDateConverter = new OracleParamToStringConverter();

        String expected = "TO_DATE(" + EXPECTED_DATE_UTC +", 'YYYY-MM-DD')";

        String renderedString = oracleDateConverter.convertToString(sqlDate);
        assertEquals(renderedString, expected, "mismatch expected formatted date string");
    }

}
