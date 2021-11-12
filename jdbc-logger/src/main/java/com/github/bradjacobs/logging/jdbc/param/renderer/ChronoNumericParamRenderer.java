package com.github.bradjacobs.logging.jdbc.param.renderer;

import com.github.bradjacobs.logging.jdbc.param.SqlParamRenderer;

import java.util.Date;

/**
 * Render any date/time/timestamp as UTC/Epoch value
 */
public class ChronoNumericParamRenderer implements SqlParamRenderer<Date>
{
    @Override
    public void appendParamValue(Date value, StringBuilder sb) {
        sb.append(value.getTime());
    }
}