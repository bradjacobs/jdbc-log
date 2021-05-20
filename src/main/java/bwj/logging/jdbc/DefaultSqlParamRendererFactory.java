package bwj.logging.jdbc;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DefaultSqlParamRendererFactory
{
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String TIME_PATTERN = "yyyy-MM-dd";

    public static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");

    private static final SqlParamRenderer DEFAULT_STRING_PARAM_RENDERER = new StringParamRenderer();
    private static final SqlParamRenderer DEFAULT_BOOLEAN_PARAM_RENDERER = new BooleanParamRenderer();


    public static Map<Class, SqlParamRenderer> createDefaultMap(String dbProductName)
    {
        return createDefaultMap(dbProductName, DEFAULT_ZONE);
    }


    public static Map<Class, SqlParamRenderer> createDefaultMap(String dbProductName, ZoneId zoneId)
    {
        if (zoneId == null) {
            zoneId = DEFAULT_ZONE;
        }

        Map<Class,SqlParamRenderer> resultMap = new HashMap<>();
        resultMap.put(String.class, DEFAULT_STRING_PARAM_RENDERER);
        resultMap.put(Boolean.class, DEFAULT_BOOLEAN_PARAM_RENDERER);

        // for default have the date/time values as a type of "string" b/c that seems most popular.
        resultMap.putAll(defaultChronoStringRenderers(zoneId));


        //  IF happen to idenfity the db verdor type, then might be able to set better defaults
        //
        // NOTE....this is all currently GUESSING!
        DatabaseType dbType = identifyDatabaseType(dbProductName);


        switch (dbType)
        {
            case SQLITE:
                resultMap.putAll(defaultChronoNumericRenderers());
                break;
            case MYSQL:
                resultMap.putAll(defaultChronoStringRenderers());
                break;
            case ORACLE:
                resultMap.put(java.sql.Timestamp.class, new OracleChronoStringParamRenderer(zoneId));
                resultMap.put(java.sql.Date.class, new OracleChronoStringParamRenderer(zoneId));
                resultMap.put(java.sql.Time.class, new OracleChronoStringParamRenderer(zoneId));
                break;
        }

        return resultMap;
    }







    public static Map<Class,SqlParamRenderer> defaultChronoNumericRenderers()
    {
        Map<Class,SqlParamRenderer> resultMap = new HashMap<>();
        resultMap.put(java.sql.Timestamp.class, new ChronoNumericParamRenderer());
        resultMap.put(java.sql.Date.class, new ChronoNumericParamRenderer());
        resultMap.put(java.sql.Time.class, new ChronoNumericParamRenderer());
        return resultMap;
    }

    public static Map<Class,SqlParamRenderer> defaultChronoStringRenderers()
    {
        return defaultChronoStringRenderers(DEFAULT_ZONE);
    }

    public static Map<Class,SqlParamRenderer> defaultChronoStringRenderers(ZoneId zoneId)
    {
        if (zoneId == null) {
            zoneId = DEFAULT_ZONE;
        }

        Map<Class,SqlParamRenderer> resultMap = new HashMap<>();
        resultMap.put(java.sql.Timestamp.class, new ChronoStringParamRenderer(DATE_TIME_PATTERN, zoneId));
        resultMap.put(java.sql.Date.class, new ChronoStringParamRenderer(DATE_PATTERN, zoneId));
        resultMap.put(java.sql.Time.class, new ChronoStringParamRenderer(TIME_PATTERN, zoneId));
        return resultMap;
    }




    // __NOT__ a complete list by any means!
    //   values in thie enum class only have meanimg if there are special param rederer types defined above.
    private static enum DatabaseType
    {
        MYSQL,
        HSQL,
        SQLITE,
        ORACLE,
        SQLSERVER,
        POSTGRES,
        // many many more

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




    private static class StringParamRenderer implements SqlParamRenderer<String>
    {
        @Override
        public void appendParamValue(String value, StringBuilder sb) {
            sb.append('\'').append(value).append('\'');
        }
    }

    private static class BooleanParamRenderer implements SqlParamRenderer<Boolean>
    {
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
