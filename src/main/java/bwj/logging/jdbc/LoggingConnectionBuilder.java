package bwj.logging.jdbc;

import bwj.logging.jdbc.param.ChronoParamRendererFactory;
import bwj.logging.jdbc.param.DefaultSqlParamRendererFactory;
import bwj.logging.jdbc.param.RendererDefinitions;
import bwj.logging.jdbc.param.RendererSelector;
import bwj.logging.jdbc.param.SqlParamRenderer;
import bwj.logging.jdbc.param.TagFiller;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LoggingConnectionBuilder
{
    private static final ZoneId DEFAULT_ZONE = DefaultSqlParamRendererFactory.DEFAULT_ZONE;


    private final ZoneId zoneId;
    private static final String tag = "?";
    private boolean logTextStreams = Boolean.FALSE;

    private final List<LoggingListener> loggingListeners = new ArrayList<>();
    private final RendererDefinitions overrideRendererDefinitions = new RendererDefinitions();

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
        overrideRendererDefinitions.setTimestampRenderer(ChronoParamRendererFactory.createDefaultTimestampParamRenderer(zoneId));
        overrideRendererDefinitions.setDateRenderer(ChronoParamRendererFactory.createDefaultDateParamRenderer(zoneId));
        overrideRendererDefinitions.setTimeRenderer(ChronoParamRendererFactory.createDefaultTimeParamRenderer(zoneId));
        return this;
    }

    /**
     * Custom string output for java.sql.Timestamp classes
     * @param pattern
     * @return
     */
    public LoggingConnectionBuilder withTimestampOnlyCustomString(String pattern) {
        overrideRendererDefinitions.setTimestampRenderer(ChronoParamRendererFactory.createChronoStringParamRenderer(pattern, zoneId));
        return this;
    }

    /**
     * Custom string output for java.sql.Date classes
     * @param pattern
     * @return
     */
    public LoggingConnectionBuilder withDateOnlyCustomString(String pattern) {
        overrideRendererDefinitions.setDateRenderer(ChronoParamRendererFactory.createChronoStringParamRenderer(pattern, zoneId));
        return this;
    }

    /**
     * Custom string output for java.sql.Time classes
     * @param pattern
     * @return
     */
    public LoggingConnectionBuilder withTimeOnlyCustomString(String pattern) {
        overrideRendererDefinitions.setTimeRenderer(ChronoParamRendererFactory.createChronoStringParamRenderer(pattern, zoneId));
        return this;
    }


    public LoggingConnectionBuilder withChronoDefaultNumerics() {
        overrideRendererDefinitions.setAllTimeDateRenderers(ChronoParamRendererFactory.createChronoNumericParamRenderer());
        return this;
    }

    /**
     * Apply ParamRender for all the date/time types
     * @param paramRenderer
     * @return
     */
    public LoggingConnectionBuilder withChronoParamRenderer(SqlParamRenderer<Date> paramRenderer) {
        overrideRendererDefinitions.setTimestampRenderer(paramRenderer);
        overrideRendererDefinitions.setDateRenderer(paramRenderer);
        overrideRendererDefinitions.setTimeRenderer(paramRenderer);
        return this;
    }




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
                RendererDefinitions rendereDefinitions = DefaultSqlParamRendererFactory.createDefaultDefinitions(dbProductName, zoneId);

                mergeInOverrideDefintions(rendereDefinitions, this.overrideRendererDefinitions);
                RendererSelector rendererSelector = new RendererSelector(rendereDefinitions);

                this.tagFiller = new TagFiller(tag, rendererSelector);
            }
        }
    }

    private void mergeInOverrideDefintions(RendererDefinitions base, RendererDefinitions override)
    {
        if (override.getDefaultRenderer() != null) {
            base.setDefaultRenderer(override.getDefaultRenderer());
        }
        if (override.getBoooleanRenderer() != null) {
            base.setBoooleanRenderer(override.getBoooleanRenderer());
        }
        if (override.getStringRenderer() != null) {
            base.setStringRenderer(override.getStringRenderer());
        }
        if (override.getTimestampRenderer() != null) {
            base.setTimestampRenderer(override.getTimestampRenderer());
        }
        if (override.getDateRenderer() != null) {
            base.setDateRenderer(override.getDateRenderer());
        }
        if (override.getTimeRenderer() != null) {
            base.setTimestampRenderer(override.getTimeRenderer());
        }
    }


}
