package bwj.logging.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoggingConnectionBuilder
{
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "yyyy-MM-dd";
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");


    private static final String DEFAULT_TAG = "?";


    private static final String tag = DEFAULT_TAG;  // ever deal w/ non-question marks?
    private boolean logTextStreams = false;

    private final List<LoggingListener> loggingListeners = new ArrayList<>();
    private final Map<Class, SqlParamRenderer> renderOverrideMap = new HashMap<>();
    private final Map<Class, SqlParamRenderer> finalRenderMap = new HashMap<>();

    private TagFiller tagFiller = null;


    public LoggingConnectionBuilder withLogListener(LoggingListener logListener) {
        loggingListeners.add(logListener);
        return this;
    }
    public LoggingConnectionBuilder setLogTextStreams(boolean logTextStreams) {
        this.logTextStreams = logTextStreams;
        return this;
    }


    // todo - begin
    //   this is a all a little kludgy clunky
    public LoggingConnectionBuilder withDatesAsDefaultStrings() {
        return
            withLogStringTimestamp(DATE_TIME_PATTERN)
            .withLogStringDate(DATE_PATTERN)
            .withLogStringTime(TIME_PATTERN);
    }


    public LoggingConnectionBuilder withLogNumericTimestamp() {
        renderOverrideMap.put(java.sql.Timestamp.class, new ChronoNumericParamRenderer());
        return this;
    }
    public LoggingConnectionBuilder withLogStringTimestamp(String pattern) {
        return this.withLogStringTimestamp(pattern, DEFAULT_ZONE);
    }
    public LoggingConnectionBuilder withLogStringTimestamp(String pattern, ZoneId zoneId) {
        renderOverrideMap.put(java.sql.Timestamp.class, new ChronoStringParamRenderer(pattern, zoneId));
        return this;
    }

    public LoggingConnectionBuilder withLogNumericDate() {
        renderOverrideMap.put(java.sql.Date.class, new ChronoNumericParamRenderer());
        return this;
    }
    public LoggingConnectionBuilder withLogStringDate(String pattern) {
        return this.withLogStringDate(pattern, DEFAULT_ZONE);
    }
    public LoggingConnectionBuilder withLogStringDate(String pattern, ZoneId zoneId) {
        renderOverrideMap.put(java.sql.Date.class, new ChronoStringParamRenderer(pattern, zoneId));
        return this;
    }

    public LoggingConnectionBuilder withLogNumericTime() {
        renderOverrideMap.put(java.sql.Time.class, new ChronoNumericParamRenderer());
        return this;
    }
    public LoggingConnectionBuilder withLogStringTime(String pattern) {
        return this.withLogStringTime(pattern, DEFAULT_ZONE);
    }
    public LoggingConnectionBuilder withLogStringTime(String pattern, ZoneId zoneId) {
        renderOverrideMap.put(java.sql.Time.class, new ChronoStringParamRenderer(pattern, zoneId));
        return this;
    }
    // todo  end


    public LoggingConnection build(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null.");
        }
        if (this.finalRenderMap.isEmpty()) {
            initializeFinalRenderMap(connection);
        }

        // todo - add validation if any params are set incorrect.

        return new LoggingConnection(connection, this.logTextStreams, this.tagFiller, this.loggingListeners);
    }



    private void initializeFinalRenderMap(Connection connection)
    {
        synchronized (this.finalRenderMap) {
            if (this.finalRenderMap.isEmpty())
            {
                // get the db provider type if possbile
                String dbProductName = null;
                try {
                    dbProductName = connection.getMetaData().getDatabaseProductName();
                }
                catch (SQLException e) {
                    /* ignore */
                }

                Map<Class, SqlParamRenderer> defaultMap = createDefaultMap(dbProductName);
                this.finalRenderMap.putAll(defaultMap);

                // add overrides over any existing default entries (if applicable)
                this.finalRenderMap.putAll(renderOverrideMap);

                this.tagFiller = new TagFiller(DEFAULT_TAG, finalRenderMap);
            }
        }
    }










    private static final SqlParamRenderer DEFAULT_STRING_RENDERER = (value, sb) -> sb.append('\'').append(value).append('\'');
    private static final SqlParamRenderer DEFAULT_BOOLEAN_RENDERER = (SqlParamRenderer<Boolean>) (value, sb) -> sb.append( (value ? 1 : 0) );

    private Map<Class, SqlParamRenderer> createDefaultMap(String dbProductName)
    {
        Map<Class,SqlParamRenderer> resultMap = new HashMap<>();
        resultMap.put(String.class, DEFAULT_STRING_RENDERER);
        resultMap.put(Boolean.class, DEFAULT_BOOLEAN_RENDERER);

        // for default have the date/time values just render as a numeric utc/epoch value.
        //
        resultMap.put(java.sql.Timestamp.class, new ChronoNumericParamRenderer());
        resultMap.put(java.sql.Date.class, new ChronoNumericParamRenderer());
        resultMap.put(java.sql.Time.class, new ChronoNumericParamRenderer());

        if (dbProductName == null) {
            return resultMap;
        }


        // if we "know" that certain vendors handle certain types specially, they could be added below
        //   so don't have to be explitly set in the Builder.
        //
        // This is primarily for the different handling of "datetime/timestamp/date/time" types.
        //      PROOF OF CONCEPT
        // (i.e. very unlikely the section below would ever be considered 'complete')

        if (dbProductName.toUpperCase().contains("MYSQL")) {

        }
        else if (dbProductName.toUpperCase().contains("ORACLE")) {
            resultMap.put(java.sql.Timestamp.class, DEFAULT_ORACLE_CHRONO_PARAM_RENDERER);
            resultMap.put(java.sql.Date.class, DEFAULT_ORACLE_CHRONO_PARAM_RENDERER);
            resultMap.put(java.sql.Time.class, DEFAULT_ORACLE_CHRONO_PARAM_RENDERER);
        }
        else if (dbProductName.toUpperCase().contains("HSQL")) {

        }
        else if (dbProductName.toUpperCase().contains("SQLITE")) {

        }
        // etc, etc.

        return resultMap;
    }



    //  GUESSING!
    //    (b/c don't have oracle db to play with)
    private static final SqlParamRenderer DEFAULT_ORACLE_CHRONO_PARAM_RENDERER = new OracleChronoStringParamRenderer();

    static class OracleChronoStringParamRenderer extends ChronoStringParamRenderer
    {
        public OracleChronoStringParamRenderer() {
            super(DATE_TIME_PATTERN, DEFAULT_ZONE);
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
