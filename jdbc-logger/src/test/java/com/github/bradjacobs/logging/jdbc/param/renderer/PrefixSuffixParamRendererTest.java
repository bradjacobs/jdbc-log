package com.github.bradjacobs.logging.jdbc.param.renderer;

import com.github.bradjacobs.logging.jdbc.param.SqlParamRenderer;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class PrefixSuffixParamRendererTest extends AbstractParamRendererTest
{
    @Test
    public void testPrefixSuffix() throws Exception
    {
        String prefix = "PRE_";
        String suffix = "_POST";
        Integer value = 77;
        String expected = prefix + value + suffix;

        SqlParamRenderer<Object> basicRenderer = new BasicParamRenderer();
        SqlParamRenderer<Object> prefixSuffixRenderer = new PrefixSuffixParamRenderer<>(basicRenderer, prefix, suffix);

        String renderedString = createRenderedString(prefixSuffixRenderer, value);
        assertEquals( renderedString, expected, "mismatch expected prefix/suffix string");
    }

    @Test
    public void testPrefixSuffix_NoPrefix() throws Exception
    {
        String prefix = "";
        String suffix = "_POST";
        Integer value = 77;
        String expected = prefix + value + suffix;

        SqlParamRenderer<Object> basicRenderer = new BasicParamRenderer();
        SqlParamRenderer<Object> prefixSuffixRenderer = new PrefixSuffixParamRenderer<>(basicRenderer, prefix, suffix);

        String renderedString = createRenderedString(prefixSuffixRenderer, value);
        assertEquals( renderedString, expected, "mismatch expected prefix/suffix string");
    }

    @Test
    public void testPrefixSuffix_NoSuffix() throws Exception
    {
        String prefix = "PRE_";
        String suffix = null;
        Integer value = 77;
        String expected = prefix + value;

        SqlParamRenderer<Object> basicRenderer = new BasicParamRenderer();
        SqlParamRenderer<Object> prefixSuffixRenderer = new PrefixSuffixParamRenderer<>(basicRenderer, prefix, suffix);

        String renderedString = createRenderedString(prefixSuffixRenderer, value);
        assertEquals( renderedString, expected, "mismatch expected prefix/suffix string");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class },
            expectedExceptionsMessageRegExp = "Renderer parameter is required.")
    public void testPrefixSuffix_BadInput() throws Exception
    {
        SqlParamRenderer<Object> prefixSuffixRenderer = new PrefixSuffixParamRenderer<>(null,  "PREFIX", "SUFFIX");
    }

}
