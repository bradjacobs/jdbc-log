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

                Map<Class, SqlParamRenderer> defaultMap = DefaultSqlParamRendererFactory.createDefaultMap(dbProductName);
                this.finalRenderMap.putAll(defaultMap);

                // add overrides over any existing default entries (if applicable)
                this.finalRenderMap.putAll(renderOverrideMap);

                this.tagFiller = new TagFiller(DEFAULT_TAG, finalRenderMap);
            }
        }
    }

}
