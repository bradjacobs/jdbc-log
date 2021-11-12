package com.github.bradjacobs.logging.jdbc.param.renderer;

import com.github.bradjacobs.logging.jdbc.param.SqlParamRenderer;
import org.apache.commons.lang3.StringUtils;

/**
 * Create renderer for string parameters.  Will quote the value and escape characters (if needed)
 */
public class StringParamRenderer implements SqlParamRenderer<String>
{
    @Override
    public void appendParamValue(String value, StringBuilder sb) {

        // escape nested quotes if necessary
        if (value.contains("'")) {
            value = StringUtils.replace(value, "'", "''");
        }
        sb.append('\'').append(value).append('\'');
    }
}