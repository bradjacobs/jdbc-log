package com.github.bradjacobs.logging.jdbc.param;


import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class SqlParamRendererGenerator
{
    private static final ZoneId DEFAULT_ZONE = RendererDefinitionsFactory.DEFAULT_ZONE;

    /**
     * Create a string param renderer for any Date type (Timestamp/Date/Time).
     *   Uses the pattern and zoneId to determine the string format.
     * @param pattern date format pattern
     * @param zoneId timezone to use for date string formatting (pass in 'null' to use the default of UTC)
     * @return renderer
     * @see java.time.format.DateTimeFormatter
     */
    public SqlParamRenderer<Date> createDateStringParamRenderer(String pattern, ZoneId zoneId)
    {
        return new ChronoStringParamRenderer(pattern, zoneId);
    }

    /**
     * Create a numeric param renderer for any Date type (Timestamp/Date/Time) as Epoch/UTC time
     * @return renderer
     */
    public SqlParamRenderer<Date> createDateNumericParamRenderer()
    {
        return new ChronoNumericParamRenderer();
    }

    /**
     * 'Decorate' an existing param renderer with anddtional prefix and suffix stirngs.
     * @param nestedRenderer the original param renderer
     * @param prefix string value to prepend
     * @param suffix string value to append.
     * @return renderer
     */
    public <T> PrefixSuffixParamRenderer<T> createPrefixSufixParamRenderer(SqlParamRenderer<T> nestedRenderer, String prefix, String suffix)
    {
        return new PrefixSuffixParamRenderer<>(nestedRenderer, prefix, suffix);
    }

    /**
     * Create super generic param renderer.  Basically will just 'toString' the object.
     * @return
     */
    public SqlParamRenderer<Object> createBasicParamRenderer()
    {
        return new BasicParamRenderer();
    }

    /**
     * Basic param renderer for booleans.  True -> ONE (1), False -> ZERO (0)
     * @return renderer
     */
    public SqlParamRenderer<Boolean> createBooleanParamRenderer()
    {
        return new BooleanParamRenderer();
    }

    /**
     * Create renderer for string parameters.  Will quote the value and escape characters (if needed)
     * @return renderer
     */
    public SqlParamRenderer<String> createStringParamRenderer()
    {
        return new StringParamRenderer();
    }




    ///////////////////////////////////////////////////////////////////////////
    //  Class declarations ...


    /**
     * Simple default.  'toString' on the object.  Typically used for numbers.
     */
    private class BasicParamRenderer implements SqlParamRenderer<Object> {
        @Override
        public void appendParamValue(Object value, StringBuilder sb) {
            sb.append(value);
        }
    }

    /**
     * String values will put inside quotes
     */
    private static class StringParamRenderer implements SqlParamRenderer<String> {
        @Override
        public void appendParamValue(String value, StringBuilder sb) {

            // escape nested quotes if necessary
            if (value.contains("'")) {
                value = StringUtils.replace(value, "'", "''");
            }
            sb.append('\'').append(value).append('\'');
        }
    }

    /**
     * Booleans will get rendered as a ZERO (0) or ONE (1)
     */
    private static class BooleanParamRenderer implements SqlParamRenderer<Boolean> {
        @Override
        public void appendParamValue(Boolean value, StringBuilder sb) {
            sb.append( (value ? 1 : 0) );
        }
    }


    private static class PrefixSuffixParamRenderer<T> implements SqlParamRenderer<T>
    {
        private final SqlParamRenderer<T> innerRenderer;
        private final String prefix;
        private final String suffix;

        public PrefixSuffixParamRenderer(SqlParamRenderer<T> renderer, String prefix, String suffix)
        {
            if (renderer == null) {
                throw new IllegalArgumentException("Renderer parameter is required.");
            }
            this.innerRenderer = renderer;
            this.prefix = prefix;
            this.suffix = suffix;
        }

        @Override
        public void appendParamValue(T value, StringBuilder sb)
        {
            if (StringUtils.isNotEmpty(prefix)) {
                sb.append(prefix);
            }
            innerRenderer.appendParamValue(value, sb);
            if (StringUtils.isNotEmpty(suffix)) {
                sb.append(suffix);
            }
        }
    }


    private static class ChronoStringParamRenderer implements SqlParamRenderer<Date>
    {
        private final DateTimeFormatter formatter;

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


    /**
     * Render any date/time/timestamp as UTC/Epoch value
     */
    private static class ChronoNumericParamRenderer implements SqlParamRenderer<Date>
    {
        @Override
        public void appendParamValue(Date value, StringBuilder sb) {
            sb.append(value.getTime());
        }
    }

}
