package bwj.logging.jdbc.param;

import bwj.logging.jdbc.DatabaseType;
import org.testng.annotations.Test;

import java.util.Date;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class RendererDefinitionsFactoryTest
{
    private static final long TEST_DATE_LONG = 1538014031000L;
    private static final Date TEST_DATE = new Date(TEST_DATE_LONG);
    private static final String EXPECTED_DATETIME_UTC = "'2018-09-27 02:07:11'";


    // when call the factory with "Oracle", then get back defaults that handles the Oracle date/timestamps differently
    @Test
    public void testOracleDefaults_Timestamp() throws Exception
    {
        RendererDefinitions rendererDefinitions = RendererDefinitionsFactory.createDefaultDefinitions(DatabaseType.identifyDatabaseType("Oracle"));

        java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(TEST_DATE_LONG);

        SqlParamRenderer<Date> renderer = rendererDefinitions.getTimestampRenderer();

        String expected = "TO_TIMESTAMP(" + EXPECTED_DATETIME_UTC + ", 'YYYY-MM-DD HH24:MI:SS')";

        String renderedString = getRenderedString(renderer, sqlTimestamp);
        assertEquals(renderedString, expected, "mismatch expected formatted date string");
    }

    // even though this is "Date" the inner part will show date and time
    @Test
    public void testOracleDefaults_Date() throws Exception
    {
        RendererDefinitions rendererDefinitions = RendererDefinitionsFactory.createDefaultDefinitions(DatabaseType.identifyDatabaseType("Oracle"));

        java.sql.Date sqlDate = new java.sql.Date(TEST_DATE_LONG);

        SqlParamRenderer<Date> renderer = rendererDefinitions.getDateRenderer();

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
