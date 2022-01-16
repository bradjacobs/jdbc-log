package com.github.bradjacobs.logging.jdbc.param.renderer;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class StringParamRendererTest extends AbstractParamRendererTest
{
    @Test
    public void testQuoteString() throws Exception
    {
        String inputString = "my test string";
        String expectedString = "'" + inputString + "'";

        StringParamRenderer renderer = new StringParamRenderer();
        assertEquals( createRenderedString(renderer, inputString), expectedString, "mismatch expected string");
    }

    @Test
    public void testEscapedQuoteString() throws Exception
    {
        String inputString = "Can't buy me love";
        String expectedString = "'" + inputString.replace("'", "''") + "'";

        StringParamRenderer renderer = new StringParamRenderer();
        assertEquals( createRenderedString(renderer, inputString), expectedString, "mismatch expected string");
    }

}
