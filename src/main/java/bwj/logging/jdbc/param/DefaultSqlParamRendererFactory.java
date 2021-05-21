package bwj.logging.jdbc.param;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

public class DefaultSqlParamRendererFactory
{
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");

    private static final SqlParamRenderer<Object> DEFAULT_PARAM_RENDERER = new BasicParamRenderer();
    private static final SqlParamRenderer<String> DEFAULT_STRING_PARAM_RENDERER = new StringParamRenderer();
    private static final SqlParamRenderer<Boolean> DEFAULT_BOOLEAN_PARAM_RENDERER = new BooleanParamRenderer();




    public static RendererDefinitions createDefaultDefinitions(String dbProductName)
    {
        return createDefaultDefinitions(dbProductName, DEFAULT_ZONE);
    }

    public static RendererDefinitions createDefaultDefinitions(String dbProductName, ZoneId zoneId)
    {
        RendererDefinitions rendererDefinitions = new RendererDefinitions();
        rendererDefinitions.setDefaultRenderer(DEFAULT_PARAM_RENDERER);
        rendererDefinitions.setStringRenderer(DEFAULT_STRING_PARAM_RENDERER);
        rendererDefinitions.setBoooleanRenderer(DEFAULT_BOOLEAN_PARAM_RENDERER);

        rendererDefinitions.setTimestampRenderer(ChronoParamRendererFactory.createDefaultTimestampParamRenderer());
        rendererDefinitions.setTimestampRenderer(ChronoParamRendererFactory.createDefaultDateParamRenderer());
        rendererDefinitions.setTimestampRenderer(ChronoParamRendererFactory.createDefaultDateParamRenderer());


        // if able to recognize the db type, can more accurately set default values.
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
                rendererDefinitions.setAllTimeDateRenderers(ChronoParamRendererFactory.createChronoNumericParamRenderer());
                break;
            case MYSQL:
                rendererDefinitions.setTimestampRenderer(ChronoParamRendererFactory.createDefaultTimestampParamRenderer());
                rendererDefinitions.setTimestampRenderer(ChronoParamRendererFactory.createDefaultDateParamRenderer());
                rendererDefinitions.setTimestampRenderer(ChronoParamRendererFactory.createDefaultDateParamRenderer());
                break;
            case ORACLE:
                rendererDefinitions.setAllTimeDateRenderers(new OracleChronoStringParamRenderer(zoneId));
                break;
        }

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




    static SqlParamRenderer<String> getDefaultStringRenderer() {
        return DEFAULT_STRING_PARAM_RENDERER;
    }
    static SqlParamRenderer<Boolean> getDefaultBooleanRenderer() {
        return DEFAULT_BOOLEAN_PARAM_RENDERER;
    }

    private static class BasicParamRenderer implements SqlParamRenderer<Object> {
        @Override
        public void appendParamValue(Object value, StringBuilder sb) {
            sb.append(value);
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



    // todo: neeeds a better home.
    private static class OracleChronoStringParamRenderer extends ChronoParamRendererFactory.ChronoStringParamRenderer<Date>
    {
        public OracleChronoStringParamRenderer(ZoneId zoneId) {
            super("yyyy-MM-dd HH:mm:ss", zoneId);
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
