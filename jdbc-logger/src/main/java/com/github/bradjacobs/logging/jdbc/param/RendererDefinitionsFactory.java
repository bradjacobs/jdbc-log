package com.github.bradjacobs.logging.jdbc.param;

import com.github.bradjacobs.logging.jdbc.DatabaseType;

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

    private static final SqlParamRendererGenerator paramRendererGenerator = new SqlParamRendererGenerator();

    private RendererDefinitionsFactory() {}


    /**
     * Generates default field renderers
     *  NOTE: all date/timestamps will be converted to strings using UTC timezone.
     * @param dbType database identifier (optional) used to help pick more 'accurate' defaults
     * @return RendererDefinitions
     */
    public static RendererDefinitions createDefaultDefinitions(DatabaseType dbType)
    {
        return createDefaultDefinitions(dbType, DEFAULT_ZONE);
    }


    /**
     * Generates default field renderers
     *  NOTE: all date/timestamps will be converted to strings using UTC timezone.
     * @param dbType database identifier (optional) used to help pick more 'accurate' defaults
     * @param zoneId a timezone for use for date string formatting (default: UTC)
     * @return RendererDefinitions
     */
    public static RendererDefinitions createDefaultDefinitions(DatabaseType dbType, ZoneId zoneId)
    {
        if (dbType == null) {
            dbType = DatabaseType.UNKNOWN;
        }
        if (zoneId == null) {
            zoneId = DEFAULT_ZONE;
        }

        // initially set with basic defaults
        SqlParamRenderer<Object> defaultRenderer = paramRendererGenerator.createBasicParamRenderer();
        SqlParamRenderer<String> stringRenderer = paramRendererGenerator.createStringParamRenderer();
        SqlParamRenderer<Boolean> booleanRenderer = paramRendererGenerator.createBooleanParamRenderer();
        SqlParamRenderer<Date> timestampRenderer = paramRendererGenerator.createDateStringParamRenderer(DEFAULT_TIMESTAMP_PATTERN, zoneId);
        SqlParamRenderer<Date> dateRenderer = paramRendererGenerator.createDateStringParamRenderer(DEFAULT_DATE_PATTERN, zoneId);
        SqlParamRenderer<Date> timeRenderer = paramRendererGenerator.createDateStringParamRenderer(DEFAULT_TIME_PATTERN, zoneId);


        // Now Check if able to recognize the Database type, which make alter
        //   the choice used for default values.
        //     ********
        //
        //          NOTE....this is all currently GUESSING!
        //
        //     ********

        switch (dbType)
        {
            case SQLITE:
                // show all the times and epoch millis
//                timestampRenderer = paramRendererGenerator.createDateNumericParamRenderer();
//                dateRenderer = paramRendererGenerator.createDateNumericParamRenderer();
//                timeRenderer = paramRendererGenerator.createDateNumericParamRenderer();
                break;
            case ORACLE:
                // NOTE: oracle really doesn't have 'time' so just assign the same as 'timestamp'
                timeRenderer = paramRendererGenerator.createPrefixSuffixParamRenderer(timestampRenderer, ORACLE_TODATE_PREFIX, ORACLE_DATETIME_SUFFIX);

                // NOTE:  currently shows a full date/time (like timestamp)
                //   argument could be made that this should only show 'date' portion, but error on the side of showing more info
                dateRenderer = paramRendererGenerator.createPrefixSuffixParamRenderer(timestampRenderer, ORACLE_TODATE_PREFIX, ORACLE_DATETIME_SUFFIX);

                //  set timestampRender last (order matters here)
                //    NOTE: it is possible to have alternate w/ fractional second precision, but this is the 'default'
                timestampRenderer = paramRendererGenerator.createPrefixSuffixParamRenderer(timestampRenderer, ORACLE_TOTIMESTAMP_PREFIX, ORACLE_DATETIME_SUFFIX);
                break;

            // fill in here as needed ......
        }

        RendererDefinitions rendererDefinitions = new RendererDefinitions();

        rendererDefinitions.setDefaultRenderer(defaultRenderer);
        rendererDefinitions.setStringRenderer(stringRenderer);
        rendererDefinitions.setBooleanRenderer(booleanRenderer);
        rendererDefinitions.setTimestampRenderer(timestampRenderer);
        rendererDefinitions.setDateRenderer(dateRenderer);
        rendererDefinitions.setTimeRenderer(timeRenderer);

        return rendererDefinitions;
    }
}
