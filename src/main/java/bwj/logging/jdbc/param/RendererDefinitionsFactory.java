package bwj.logging.jdbc.param;

import java.time.ZoneId;
import java.util.Date;

public class RendererDefinitionsFactory
{
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");


    private static final String DEFAULT_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    private static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";

    private static final String ORACLE_TOTIMESTAMP_PREFIX = "TO_TIMESTAMP(";
    private static final String ORACLE_TODATE_PREFIX = "TO_DATE(";
    private static final String ORACLE_DATETIME_SUFFIX = ", 'YYYY-MM-DD HH24:MI:SS')";

    private static final SqlParamRendererGenerator paramRendererGenerstor = new SqlParamRendererGenerator();

    private RendererDefinitionsFactory() {}

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
        SqlParamRenderer<Object> defaultRenderer = paramRendererGenerstor.createBasicParamRenderer();
        SqlParamRenderer<String> stringRenderer = paramRendererGenerstor.createStringParamRenderer();
        SqlParamRenderer<Boolean> booleanRenderer = paramRendererGenerstor.createBooleanParamRenderer();
        SqlParamRenderer<Date> timestampRenderer = paramRendererGenerstor.createDateStringParamRenderer(DEFAULT_TIMESTAMP_PATTERN, zoneId);
        SqlParamRenderer<Date> dateRenderer = paramRendererGenerstor.createDateStringParamRenderer(DEFAULT_DATE_PATTERN, zoneId);
        SqlParamRenderer<Date> timeRenderer = paramRendererGenerstor.createDateStringParamRenderer(DEFAULT_TIME_PATTERN, zoneId);


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
                timestampRenderer = paramRendererGenerstor.createDateNumericParamRenderer();
                dateRenderer = paramRendererGenerstor.createDateNumericParamRenderer();
                timeRenderer = paramRendererGenerstor.createDateNumericParamRenderer();
                break;
            case ORACLE:
                // NOTE: oracle really dosn't have 'time' so just assign the same as 'timestamp'
                timeRenderer = paramRendererGenerstor.createPrefixSufixParamRenderer(timestampRenderer, ORACLE_TODATE_PREFIX, ORACLE_DATETIME_SUFFIX);

                // NOTE:  currently shows a full date/time (like timestamp)
                //   argument could be made that this should only show 'date' portion, but error on the side of showing more info
                dateRenderer = paramRendererGenerstor.createPrefixSufixParamRenderer(timestampRenderer, ORACLE_TODATE_PREFIX, ORACLE_DATETIME_SUFFIX);

                //  set timestampRender last (or matters here)
                //    NOTE: it is possible to have alternate w/ fractional second precision, but this is the 'default'
                timestampRenderer = paramRendererGenerstor.createPrefixSufixParamRenderer(timestampRenderer, ORACLE_TOTIMESTAMP_PREFIX, ORACLE_DATETIME_SUFFIX);
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
     * @return DatabaseType based on input parameter
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
}
