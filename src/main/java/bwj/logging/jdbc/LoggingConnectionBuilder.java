package bwj.logging.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoggingConnectionBuilder
{
    private static final ZoneId DEFAULT_ZONE = DefaultSqlParamRendererFactory.DEFAULT_ZONE;


    private final ZoneId zoneId;
    private static final String tag = "?";
    private boolean logTextStreams = Boolean.FALSE;

    private final List<LoggingListener> loggingListeners = new ArrayList<>();
    private final Map<Class, SqlParamRenderer> renderOverrideMap = new HashMap<>();

    private TagFiller tagFiller = null;

    public LoggingConnectionBuilder()
    {
        this(DEFAULT_ZONE);
    }


    /**
     * can Optionally supply a custom timezone
     *    (used for rendering dates and timestamps to strings)
     * Note: "ZoneId.systemDefault" can be used for the current local timezone.
     * @param zoneId
     */
    public LoggingConnectionBuilder(ZoneId zoneId)
    {
        // if explicitly passed in a null, change to default
        if (zoneId == null) {
            zoneId = DEFAULT_ZONE;
        }
        this.zoneId = zoneId;
    }

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

    /**
     * Sets the date/time class with default string pattern output
     *  java.sql.Timestamp -> yyyy-MM-dd HH:mm:ss
     *  java.sql.Date -> yyyy-MM-dd
     *  java.sql.Time -> HH:mm:ss
     * @return
     */
    public LoggingConnectionBuilder withChronoDefaultStrings() {
        renderOverrideMap.putAll(DefaultSqlParamRendererFactory.defaultChronoStringRenderers(this.zoneId));
        return this;
    }

    /**
     * Custom string output for java.sql.Timestamp classes
     * @param pattern
     * @return
     */
    public LoggingConnectionBuilder withTimestampOnlyCustomString(String pattern) {
        renderOverrideMap.putAll(DefaultSqlParamRendererFactory.customTimestampString(pattern, this.zoneId));
        return this;
    }

    /**
     * Custom string output for java.sql.Date classes
     * @param pattern
     * @return
     */
    public LoggingConnectionBuilder withDateOnlyCustomString(String pattern) {
        renderOverrideMap.putAll(DefaultSqlParamRendererFactory.customDateString(pattern, this.zoneId));
        return this;
    }

    /**
     * Custom string output for java.sql.Time classes
     * @param pattern
     * @return
     */
    public LoggingConnectionBuilder withTimeOnlyCustomString(String pattern) {
        renderOverrideMap.putAll(DefaultSqlParamRendererFactory.customTimeString(pattern, this.zoneId));
        return this;
    }



    public LoggingConnectionBuilder withChronoDefaultNumerics() {
        renderOverrideMap.putAll(DefaultSqlParamRendererFactory.defaultChronoNumericRenderers());
        return this;
    }

    /**
     * Apply ParamRender for all the date/time types
     * @param paramRenderer
     * @return
     */
    public <T extends Date> LoggingConnectionBuilder withChronoParamRenderer(SqlParamRenderer<T> paramRenderer) {
        if (paramRenderer != null) {
            for (Class clz : DefaultSqlParamRendererFactory.getDateTimeClasses()) {
                renderOverrideMap.put(clz, paramRenderer);
            }
        }
        return this;
    }

    public <T> LoggingConnectionBuilder withParamRenderer(Class<T> clazz, SqlParamRenderer<T> paramRenderer) {
        if (clazz != null && paramRenderer != null) {
            renderOverrideMap.put(clazz, paramRenderer);
        }
        return this;
    }
    // todo  end



    public LoggingConnection build(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null.");
        }
        if (this.tagFiller == null) {
            initializeFinalRenderMap(connection);
        }

        // todo - add validation if any params are set incorrect.

        return new LoggingConnection(connection, this.logTextStreams, this.tagFiller, this.loggingListeners);
    }



    private void initializeFinalRenderMap(Connection connection)
    {
        synchronized (this) {
            if (this.tagFiller == null)
            {
                // get the db provider type if possbile
                String dbProductName = null;
                try {
                    dbProductName = connection.getMetaData().getDatabaseProductName();
                }
                catch (SQLException e) {
                    // todo: should log error
                    /* ignore */
                }

                // create initial map w/ default values
                Map<Class, SqlParamRenderer> paramRenderMap = DefaultSqlParamRendererFactory.createDefaultMap(dbProductName, this.zoneId);

                // add overrides (that can overrule any existing map entries)
                paramRenderMap.putAll(renderOverrideMap);

                this.tagFiller = new TagFiller(tag, paramRenderMap);
            }
        }
    }

}
