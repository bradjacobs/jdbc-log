package bwj.logging.jdbc.param;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefaultSqlParamRendererFactory
{
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String TIME_PATTERN = "HH:mm:ss";

    public static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");

    private static final SqlParamRenderer DEFAULT_STRING_PARAM_RENDERER = new StringParamRenderer();
    private static final SqlParamRenderer DEFAULT_BOOLEAN_PARAM_RENDERER = new BooleanParamRenderer();


    private static final Set<Class> dateTimeClasses =
        Collections.unmodifiableSet(new HashSet<Class>(
            Arrays.asList(
                java.sql.Timestamp.class,
                java.sql.Date.class,
                java.sql.Time.class)
        ));

    public static Set<Class> getDateTimeClasses() {
        return dateTimeClasses;
    }



    public static Map<Class, SqlParamRenderer> createDefaultMap(String dbProductName)
    {
        return createDefaultMap(dbProductName, DEFAULT_ZONE);
    }

    public static Map<Class, SqlParamRenderer> createDefaultMap(String dbProductName, ZoneId zoneId)
    {
        Map<Class,SqlParamRenderer> resultMap = new HashMap<>();
        resultMap.put(String.class, DEFAULT_STRING_PARAM_RENDERER);
        resultMap.put(Boolean.class, DEFAULT_BOOLEAN_PARAM_RENDERER);

        // for date/time "defaults" have the values render as a type of "string" b/c that seems most popular.
        resultMap.putAll(defaultChronoStringRenderers(zoneId));


        //  IF happen to idenfity the db vendor type, then might be able to set better defaults
        //
        //    NOTE....this is all currently GUESSING!
        DatabaseType dbType = identifyDatabaseType(dbProductName);


        switch (dbType)
        {
            case SQLITE:
                resultMap.putAll(defaultChronoNumericRenderers());
                break;
            case MYSQL:
                resultMap.putAll(defaultChronoStringRenderers(zoneId));
                break;
            case ORACLE:
                for (Class dateTimeClass : dateTimeClasses) {
                    resultMap.put(dateTimeClass, new OracleChronoStringParamRenderer(zoneId));
                }
                break;
        }

        return resultMap;
    }




    public static Map<Class,SqlParamRenderer> defaultChronoNumericRenderers()
    {
        Map<Class,SqlParamRenderer> resultMap = new HashMap<>();
        for (Class dateTimeClass : dateTimeClasses) {
            resultMap.put(dateTimeClass, new ChronoNumericParamRenderer());
        }

        return resultMap;
    }

    public static Map<Class,SqlParamRenderer> defaultChronoStringRenderers(ZoneId zoneId)
    {
        Map<Class,SqlParamRenderer> resultMap = new HashMap<>();
        resultMap.putAll(defaultTimestampString(zoneId));
        resultMap.putAll(defaultDateString(zoneId));
        resultMap.putAll(defaultTimeString(zoneId));
        return resultMap;
    }

    public static Map<Class,SqlParamRenderer> defaultTimestampString(ZoneId zoneId) {
        return customTimestampString(DATE_TIME_PATTERN, zoneId);
    }
    public static Map<Class,SqlParamRenderer> defaultDateString(ZoneId zoneId) {
        return customTimestampString(DATE_PATTERN, zoneId);
    }
    public static Map<Class,SqlParamRenderer> defaultTimeString(ZoneId zoneId) {
        return customTimestampString(TIME_PATTERN, zoneId);
    }

    public static Map<Class,SqlParamRenderer> customTimestampString(String pattern, ZoneId zoneId) {
        return Collections.singletonMap(java.sql.Timestamp.class, new ChronoStringParamRenderer(pattern, zoneId));
    }
    public static Map<Class,SqlParamRenderer> customDateString(String pattern, ZoneId zoneId) {
        return Collections.singletonMap(java.sql.Date.class, new ChronoStringParamRenderer(pattern, zoneId));
    }
    public static Map<Class,SqlParamRenderer> customTimeString(String pattern, ZoneId zoneId) {
        return Collections.singletonMap(java.sql.Time.class, new ChronoStringParamRenderer(pattern, zoneId));
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

        if (dbName.contains("MYSQL")) {
            return DatabaseType.MYSQL;
        }
        else if (dbName.contains("HSQL")) {
            return DatabaseType.HSQL;
        }
        else if (dbName.contains("SQLITE")) {
            return DatabaseType.SQLITE;
        }
        else if (dbName.contains("ORACLE")) {
            return DatabaseType.ORACLE;
        }
        else if (dbName.contains("POSTGRES")) {
            return DatabaseType.POSTGRES;
        }
        else if (dbName.contains("SQLSERVER") || dbName.contains("SQL SERVER")) {
            return DatabaseType.SQLSERVER;
        }
        else {
            return DatabaseType.UNKNOWN;
        }
    }




    private static class StringParamRenderer implements SqlParamRenderer<String> {
        @Override
        public void appendParamValue(String value, StringBuilder sb) {
            sb.append('\'').append(value).append('\'');
        }
    }

    private static class BooleanParamRenderer implements SqlParamRenderer<Boolean> {
        @Override
        public void appendParamValue(Boolean value, StringBuilder sb) {
            sb.append( (value ? 1 : 0) );
        }
    }



    private static class OracleChronoStringParamRenderer extends ChronoStringParamRenderer
    {
        public OracleChronoStringParamRenderer(ZoneId zoneId) {
            super(DATE_TIME_PATTERN, zoneId);
        }

        @Override
        public void appendParamValue(Date value, StringBuilder sb)
        {
            sb.append("to_date(");
            sb.append('\'');
            sb.append(formatter.format(Instant.ofEpochMilli(value.getTime())));
            sb.append('\'');
            sb.append(", 'YYYY-MM-DD HH24:MI:SS')");
        }
    }


}
