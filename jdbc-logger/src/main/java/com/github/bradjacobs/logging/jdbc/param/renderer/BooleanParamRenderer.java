package com.github.bradjacobs.logging.jdbc.param.renderer;

import com.github.bradjacobs.logging.jdbc.param.SqlParamRenderer;

/**
 * Booleans will get rendered as a ZERO (0) or ONE (1)
 */
public class BooleanParamRenderer implements SqlParamRenderer<Boolean>
{
    @Override
    public void appendParamValue(Boolean value, StringBuilder sb) {
        sb.append( (value ? 1 : 0) );
    }
}
