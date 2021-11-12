package com.github.bradjacobs.logging.jdbc.param.renderer;

import com.github.bradjacobs.logging.jdbc.param.SqlParamRenderer;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.github.bradjacobs.logging.jdbc.param.ParamRendererFactory.DEFAULT_ZONE;

public class ChronoStringParamRenderer implements SqlParamRenderer<Date>
{
    private final DateTimeFormatter formatter;

    /**
     * Create a string param renderer for any Date type (Timestamp/Date/Time).
     *   Uses the pattern and zoneId to determine the string format.
     * @param pattern date format pattern
     * @param zoneId timezone to use for date string formatting (pass in 'null' to use the default of UTC)
     * @see java.time.format.DateTimeFormatter
     */
    public ChronoStringParamRenderer(String pattern, ZoneId zoneId)
    {
        if (StringUtils.isEmpty(pattern)) {
            throw new IllegalArgumentException("datetime formatter pattern is required.");
        }
        if (zoneId == null) {
            zoneId = DEFAULT_ZONE;
        }

        // give a slightly more meaningful error msg with bad parameter.
        try {
            formatter = DateTimeFormatter.ofPattern(pattern).withZone(zoneId);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Invalid DateFormat pattern: '%s'. Error: %s.", pattern, e.getMessage()), e);
        }
    }

    @Override
    public void appendParamValue(Date value, StringBuilder sb)
    {
        sb.append('\'');
        sb.append(formatter.format(Instant.ofEpochMilli(value.getTime())));
        sb.append('\'');
    }
}
