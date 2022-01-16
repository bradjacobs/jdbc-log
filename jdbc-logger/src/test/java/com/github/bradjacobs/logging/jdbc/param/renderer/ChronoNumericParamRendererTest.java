package com.github.bradjacobs.logging.jdbc.param.renderer;

import org.testng.annotations.Test;

import java.util.Date;

import static org.testng.Assert.assertEquals;

public class ChronoNumericParamRendererTest extends AbstractParamRendererTest
{
    private static final long TEST_DATE_LONG = 1538014031000L;
    private static final Date TEST_DATE = new Date(TEST_DATE_LONG);

    @Test
    public void testDateNumeric() throws Exception
    {
        java.sql.Time sqlTime = new java.sql.Time(TEST_DATE_LONG);
        String expectedString = String.valueOf(TEST_DATE_LONG);
        ChronoNumericParamRenderer renderer = new ChronoNumericParamRenderer();

        assertEquals( createRenderedString(renderer, sqlTime), expectedString, "mismatch expected date string");
    }
}
