package com.github.bradjacobs.logging.jdbc.param.renderer;

import com.github.bradjacobs.logging.jdbc.param.SqlParamRenderer;

/**
 * Simple default.  'toString' on the object.
 */
public class BasicParamRenderer implements SqlParamRenderer<Object>
{
    @Override
    public void appendParamValue(Object value, StringBuilder sb) {
        sb.append(value);
    }
}
