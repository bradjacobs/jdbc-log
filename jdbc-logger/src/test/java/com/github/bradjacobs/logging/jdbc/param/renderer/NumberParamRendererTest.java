package com.github.bradjacobs.logging.jdbc.param.renderer;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class NumberParamRendererTest extends AbstractParamRendererTest
{
    @Test
    public void baseCaseNumber() throws Exception
    {
        Long inputValue = 931L;
        String expectedString = String.valueOf(inputValue);

        NumberParamRenderer renderer = new NumberParamRenderer();
        assertEquals( createRenderedString(renderer, inputValue), expectedString, "mismatch expected number string");
    }

    /**
     * Test that double does _NOT_ show up like 1.2E-7
     */
    @Test
    public void testNoScientificNotation() throws Exception
    {
        Double inputValue = 0.00000012d;
        String expectedString = "0.00000012";

        NumberParamRenderer renderer = new NumberParamRenderer();
        assertEquals( createRenderedString(renderer, inputValue), expectedString, "mismatch expected number string");
    }
}
