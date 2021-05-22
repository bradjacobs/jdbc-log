package bwj.logging.jdbc.param;


import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class SqlParamRendererFactory
{
    private static final String DEFAULT_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    private static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");


    private SqlParamRendererFactory() { }



    public static SqlParamRenderer<Date> createDateStringParamRenderer(String pattern, ZoneId zoneId)
    {
        return new ChronoStringParamRenderer(pattern, zoneId);
    }
    public static SqlParamRenderer<Date> createDateNumericDParamRenderer()
    {
        return new ChronoNumericParamRenderer();
    }


    /**
     * Generates default field renderers
     *  NOTE: all date/timestamps will be converted to strings using UTC timezone.
     * @param dbProductName database name identifier (optional)
     *                      used to help pick more 'accurate' defaults
     * @return RendererDefinitions
     */
    public static RendererDefinitions createDefaultDefinitions(String dbProductName)
    {
        return createDefaultDefinitions(dbProductName, DEFAULT_ZONE);
    }

    /**
     * Generates default field renderers
     *  NOTE: all date/timestamps will be converted to strings using UTC timezone.
     * @param dbProductName database name identifier (optional)
     *                      used to help pick more 'accurate' defaults
     * @param zoneId a timezone for use for date string formatting (default: UTC)
     * @return RendererDefinitions
     */
    public static RendererDefinitions createDefaultDefinitions(String dbProductName, ZoneId zoneId)
    {
        if (zoneId == null) {
            zoneId = DEFAULT_ZONE;
        }

        // initially set with basic defaults
        SqlParamRenderer<Object> defaultRenderer = DEFAULT_PARAM_RENDERER;
        SqlParamRenderer<String> stringRenderer = DEFAULT_STRING_PARAM_RENDERER;
        SqlParamRenderer<Boolean> booleanRenderer = DEFAULT_BOOLEAN_PARAM_RENDERER;
        SqlParamRenderer<Date> timestampRenderer = createDateStringParamRenderer(DEFAULT_TIMESTAMP_PATTERN, zoneId);
        SqlParamRenderer<Date> dateRenderer = createDateStringParamRenderer(DEFAULT_DATE_PATTERN, zoneId);
        SqlParamRenderer<Date> timeRenderer = createDateStringParamRenderer(DEFAULT_TIME_PATTERN, zoneId);


        // Now Check if able to recognize the Database type, which make alter
        //   the choice used for default values.
        //     ********
        //
        //          NOTE....this is all currently GUESSING!
        //
        //     ********
        DatabaseType dbType = identifyDatabaseType(dbProductName);

        switch (dbType)
        {
            case SQLITE:
                // show all the times and epoch millis
                timestampRenderer = new ChronoNumericParamRenderer();
                dateRenderer = new ChronoNumericParamRenderer();
                timeRenderer = new ChronoNumericParamRenderer();
                break;
            case ORACLE:
                // oracle really dosn't have 'time' so just assign same as 'date'
                timeRenderer = new PrefixSuffixParamRenderer<Date>(timestampRenderer, ORACLE_DATE_PREFIX, ORACLE_SUFFIX);

                // yes, it is correct that the dateRenderer will user the inner timeestamp  (for  yyyy-MM-dd HH:mm:ss)
                dateRenderer = new PrefixSuffixParamRenderer<Date>(timestampRenderer, ORACLE_DATE_PREFIX, ORACLE_SUFFIX);

                //  set timestamRender last (or matters here)
                //    NOTE: it is possible to have alternate w/ fractional second precision, but this is 'default'
                timestampRenderer = new PrefixSuffixParamRenderer<Date>(timestampRenderer, ORACLE_TS_PREFIX, ORACLE_SUFFIX);
                break;

            // fill in here as needed ......
        }

        RendererDefinitions rendererDefinitions = new RendererDefinitions();

        rendererDefinitions.setDefaultRenderer(defaultRenderer);
        rendererDefinitions.setStringRenderer(stringRenderer);
        rendererDefinitions.setBoooleanRenderer(booleanRenderer);
        rendererDefinitions.setTimestampRenderer(timestampRenderer);
        rendererDefinitions.setDateRenderer(dateRenderer);
        rendererDefinitions.setTimeRenderer(timeRenderer);

        return rendererDefinitions;
    }




    // This is __NOT__ a complete list by any means!
    //   values in thie enum class only have meaning if there are special param renderer types defined above.
    private enum DatabaseType
    {
        MYSQL,
        HSQL,
        SQLITE,
        ORACLE,
        SQLSERVER,
        POSTGRES,
        // many many more ...

        UNKNOWN,
    }


    /**
     * Attempt to identify db type via string
     * @param dbName either database product name or driver name.
     * @return
     */
    private static DatabaseType identifyDatabaseType(String dbName)
    {
        if (dbName == null) {
            return DatabaseType.UNKNOWN;
        }

        dbName = dbName.toUpperCase();
        if (dbName.contains("MYSQL")) { return DatabaseType.MYSQL; }
        else if (dbName.contains("HSQL")) { return DatabaseType.HSQL; }
        else if (dbName.contains("SQLITE")) { return DatabaseType.SQLITE; }
        else if (dbName.contains("ORACLE")) { return DatabaseType.ORACLE; }
        else if (dbName.contains("POSTGRES")) { return DatabaseType.POSTGRES; }
        else if (dbName.contains("SQLSERVER") || dbName.contains("SQL SERVER")) { return DatabaseType.SQLSERVER; }
        else { return DatabaseType.UNKNOWN; }
    }


    ///////////////////////////////////////////////////////////////////////////
    //  Default declarations ...

    private static final SqlParamRenderer<Object> DEFAULT_PARAM_RENDERER = new BasicParamRenderer();
    private static final SqlParamRenderer<String> DEFAULT_STRING_PARAM_RENDERER = new StringParamRenderer();
    private static final SqlParamRenderer<Boolean> DEFAULT_BOOLEAN_PARAM_RENDERER = new BooleanParamRenderer();


    private static final String ORACLE_TS_PREFIX = "TO_TIMESTAMP(";
    private static final String ORACLE_DATE_PREFIX = "TO_DATE(";
    private static final String ORACLE_SUFFIX = ", 'YYYY-MM-DD HH24:MI:SS')";


    /**
     * Simple default.  'toString' on the object.  Typically used for numbers.
     */
    private static class BasicParamRenderer implements SqlParamRenderer<Object> {
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


    public static class PrefixSuffixParamRenderer<T> implements SqlParamRenderer<T>
    {
        private final SqlParamRenderer<T> innerRenderer;
        private final String prefix;
        private final String suffix;

        public PrefixSuffixParamRenderer(SqlParamRenderer<T> renderer, String prefix, String suffix)
        {
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
            }        }

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
