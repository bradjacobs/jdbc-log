package com.github.bradjacobs.logging.jdbc.param.rendener;

import com.github.bradjacobs.logging.jdbc.param.SqlParamRenderer;

import static org.testng.Assert.assertNotNull;

abstract public class AbstractParamRendererTest
{
    protected <T> String createRenderedString(SqlParamRenderer<T> renderer, T d)
    {
        assertNotNull(renderer, "expected non-null renderer object");
        StringBuilder sb = new StringBuilder();
        renderer.appendParamValue(d, sb);
        return sb.toString();
    }

}
