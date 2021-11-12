package com.github.bradjacobs.logging.jdbc.param.rendener;

import com.github.bradjacobs.logging.jdbc.param.renderer.BooleanParamRenderer;
import com.github.bradjacobs.logging.jdbc.param.renderer.StringParamRenderer;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class BooleanParamRendererTest extends AbstractParamRendererTest
{
    @Test
    public void testTrue() throws Exception
    {
        Boolean input = true;
        String expectedString = "1";

        BooleanParamRenderer renderer = new BooleanParamRenderer();
        assertEquals( createRenderedString(renderer, input), expectedString, "mismatch expected string");
    }

    @Test
    public void testFalse() throws Exception
    {
        Boolean input = false;
        String expectedString = "0";

        BooleanParamRenderer renderer = new BooleanParamRenderer();
        assertEquals( createRenderedString(renderer, input), expectedString, "mismatch expected string");
    }
}
