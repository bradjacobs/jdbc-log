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

/**
 *
 *
 * @see <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html">DateTimeFormatter</a>
 */
public class LoggingConnectionBuilder
{
    private static final ZoneId DEFAULT_ZONE = DefaultSqlParamRendererFactory.DEFAULT_ZONE;


    private final ZoneId zoneId;
    private static final String tag = "?";
    private boolean streamLoggingEnabled = Boolean.FALSE;

    private final List<LoggingListener> loggingListeners = new ArrayList<>();
    private final RendererDefinitions overrideRendererDefinitions = new RendererDefinitions();

    private TagFiller tagFiller = null;

    /**
     * Default LoggingConnectionBuilder constructor
     *   will use "UTC" timezone for any Date/Timesteamp string value formatting.
     */
    public LoggingConnectionBuilder()
    {
        this(DEFAULT_ZONE);
    }

    /**
     * LoggingConnectionBuilder constructor
     * Optionally supply a custom timezone (used any Date/Timesteamp string value formatting.)
     *    note: "ZoneId.systemDefault" can be used for the current local timezone.
     * @param zoneId ( default: UTC )
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

    /**
     * Enables to abiltty to log Text Clob/Reader/InputStream values
     *    WARNING: can significantly impact performance if enabled.
     * @param streamLoggingEnabled  (default: FALSE)
     */
    public LoggingConnectionBuilder setStreamLoggingEnabled(boolean streamLoggingEnabled) {
        this.streamLoggingEnabled = streamLoggingEnabled;
        return this;
    }


    /**
     * Configures Timestamps, Dates, Times to rendered as strings with default format pattern
     *   i.e. "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", and "HH:mm:ss" respectively
     */
    public LoggingConnectionBuilder withChronoDefaultStrings() {
        overrideRendererDefinitions.setTimestampRenderer(ChronoParamRendererFactory.createDefaultTimestampParamRenderer(zoneId));
        overrideRendererDefinitions.setDateRenderer(ChronoParamRendererFactory.createDefaultDateParamRenderer(zoneId));
        overrideRendererDefinitions.setTimeRenderer(ChronoParamRendererFactory.createDefaultTimeParamRenderer(zoneId));
        return this;
    }

    /**
     * Custom date format string pattern for rendering java.sql.Timestamp as strings
     * @param pattern date format pattern
     * @see java.time.format.DateTimeFormatter
     */
    public LoggingConnectionBuilder withTimestampOnlyCustomString(String pattern) {
        overrideRendererDefinitions.setTimestampRenderer(ChronoParamRendererFactory.createChronoStringParamRenderer(pattern, zoneId));
        return this;
    }

    /**
     * Custom date format string pattern for rendering java.sql.Date as strings
     * @param pattern date format pattern
     * @see java.time.format.DateTimeFormatter
     */
    public LoggingConnectionBuilder withDateOnlyCustomString(String pattern) {
        overrideRendererDefinitions.setDateRenderer(ChronoParamRendererFactory.createChronoStringParamRenderer(pattern, zoneId));
        return this;
    }

    /**
     * Custom date format string pattern for rendering java.sql.Time as strings
     * @param pattern date format pattern
     * @see java.time.format.DateTimeFormatter
     */
    public LoggingConnectionBuilder withTimeOnlyCustomString(String pattern) {
        overrideRendererDefinitions.setTimeRenderer(ChronoParamRendererFactory.createChronoStringParamRenderer(pattern, zoneId));
        return this;
    }


    /**
     * Enable all timestamp/date/time instandes to rendered as numeric (unix/epoch time)
     */
    public LoggingConnectionBuilder withChronoDefaultNumerics() {
        overrideRendererDefinitions.setAllTimeDateRenderers(ChronoParamRendererFactory.createChronoNumericParamRenderer());
        return this;
    }

    // todo - fix: method below could cause ClassCastExxeption
    /**
     * Apply ParamRender for all the date/time types
     * @param paramRenderer paramRenderer
     */
    public <T extends Date> LoggingConnectionBuilder withChronoParamRenderer(SqlParamRenderer<T> paramRenderer) {
        overrideRendererDefinitions.setTimestampRenderer(paramRenderer);
        overrideRendererDefinitions.setDateRenderer(paramRenderer);
        overrideRendererDefinitions.setTimeRenderer(paramRenderer);
        return this;
    }


    /**
     * Creates a LoggingConnection
     * @param connection the original connection to be wrapped with LoggingConnection.
     * @return loggingConnection
     */
    public LoggingConnection build(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null.");
        }
        if (this.tagFiller == null) {
            initializeFinalRenderMap(connection);
        }

        return new LoggingConnection(connection, this.streamLoggingEnabled, this.tagFiller, this.loggingListeners);
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

                // create initial defn's w/ defaul values
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
