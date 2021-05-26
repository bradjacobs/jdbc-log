package bwj.logging.jdbc;

import bwj.logging.jdbc.listeners.SoutLogListener;
import bwj.logging.jdbc.param.RendererDefinitions;
import bwj.logging.jdbc.param.RendererDefinitionsFactory;
import bwj.logging.jdbc.param.RendererSelector;
import bwj.logging.jdbc.param.SqlParamRenderer;
import bwj.logging.jdbc.param.SqlParamRendererGenerator;
import bwj.logging.jdbc.param.TagFiller;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 *
 *
 * @see <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html">DateTimeFormatter</a>
 */
public class LoggingSqlConfig
{
    private final boolean isStreamLoggging;
    private final List<LoggingListener> loggingListeners;
    private final TagFiller tagFiller;
    private final DatabaseType dbType;


    private LoggingSqlConfig(Builder builder)
    {
        this.dbType = builder.dbType;  // note: is only used for informtational purposes
        this.isStreamLoggging = builder.streamLogging;
        this.loggingListeners = Collections.unmodifiableList(builder.loggingListeners);
        this.tagFiller = builder.tagFiller;
    }

    public boolean isStreamLoggging()
    {
        return isStreamLoggging;
    }

    public List<LoggingListener> getLoggingListeners()
    {
        return loggingListeners;
    }

    public TagFiller getTagFiller()
    {
        return tagFiller;
    }

    public DatabaseType getDbType()
    {
        return dbType;
    }




    public static class Builder {
        private static final SqlParamRendererGenerator paramRendererGenerator = new SqlParamRendererGenerator();

        private static final String TAG = "?";
        private static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");
        private static final Boolean DEFAULT_STREAM_LOGGING = Boolean.FALSE;
        private static final LoggingListener DEFAULT_LOGGING_LISTENER = new SoutLogListener(); // used only if no listeners are provided.

        // note: force the zoneId to get set first (on constructor)
        //   otherwise order can matter when setting some of the other fields.
        private final ZoneId zoneId;


        private String dbTypeIdentifier = null;

        private final List<LoggingListener> loggingListeners = new ArrayList<>();
        private final RendererDefinitions overrideRendererDefinitions = new RendererDefinitions();
        private boolean streamLogging = DEFAULT_STREAM_LOGGING;


        // assigned during 'build'
        private DatabaseType dbType;
        private TagFiller tagFiller;


        /**
         * LoggingSqlConfig.Builder constructor
         */
        public Builder() {
            this(DEFAULT_ZONE);
        }

        /**
         * LoggingSqlConfig.Builder constructor
         *   Can optionally supply a custom timezone (used any Date/Timesteamp string value formatting.)
         *     (note: 'ZoneId.systemDefault' can be used for the current local timezone
         * @param zoneId timeeZone  (default: UTC)
         */
        public Builder(ZoneId zoneId) {
            if (zoneId == null) {
                zoneId = DEFAULT_ZONE;
            }
            this.zoneId = zoneId;
        }


        /**
         * Any string value that can help identify which type of database is used.  (MySQL, HSQL, PostGres, etc, etc)
         *   Could be JDBC url, driver class name, hardocded string.
         * The config will look for specific 'substrings' to determine database type.
         *   (i.e.  if contains 'mysql', must be a MySQL database)
         * @param dbTypeIdentifier string identifying database used.  If not set then 'general database defaults' will be selected.
         */
        public Builder withDbTypeIdentifier(String dbTypeIdentifier) {
            this.dbTypeIdentifier= dbTypeIdentifier;
            return this;
        }


        public Builder withLogListener(LoggingListener logListener) {
            if (logListener == null) {
                throw new IllegalArgumentException("Cannot set a logging listener to null.");
            }
            loggingListeners.add(logListener);
            return this;
        }


        /**
         * Enables abiltty to log Text Clob/Reader/InputStream values
         *    WARNING: could significantly impact performance if enabled.
         * @param streamLoggingEnabled  (default: FALSE)
         */
        public Builder setStreamLoggingEnabled(boolean streamLoggingEnabled) {
            this.streamLogging = streamLoggingEnabled;
            return this;
        }



        /**
         * Custom date format string pattern for rendering java.sql.Timestamp as strings
         * @param pattern date format pattern
         * @see java.time.format.DateTimeFormatter
         */
        public Builder withTimestampOnlyCustomPattern(String pattern) {
            overrideRendererDefinitions.setTimestampRenderer(paramRendererGenerator.createDateStringParamRenderer(pattern, zoneId));
            return this;
        }

        /**
         * Custom date format string pattern for rendering java.sql.Date as strings
         * @param pattern date format pattern
         * @see java.time.format.DateTimeFormatter
         */
        public Builder withDateOnlyCustomPattern(String pattern) {
            overrideRendererDefinitions.setDateRenderer(paramRendererGenerator.createDateStringParamRenderer(pattern, zoneId));
            return this;
        }

        /**
         * Custom date format string pattern for rendering java.sql.Time as strings
         * @param pattern date format pattern
         * @see java.time.format.DateTimeFormatter
         */
        public Builder withTimeOnlyCustomPattern(String pattern) {
            overrideRendererDefinitions.setTimeRenderer(paramRendererGenerator.createDateStringParamRenderer(pattern, zoneId));
            return this;
        }


        /**
         * Enable all timestamp/date/time instanes to rendered as numeric (unix/epoch time)
         */
        public Builder withChronoDefaultNumerics() {
            return withChronoParamRenderer(paramRendererGenerator.createDateNumericParamRenderer());
        }

        /**
         * Apply ParamRender for all the timestamp/date/time types
         * @param paramRenderer paramRenderer
         */
        public Builder withChronoParamRenderer(SqlParamRenderer<Date> paramRenderer) {
            overrideRendererDefinitions.setTimestampRenderer(paramRenderer);
            overrideRendererDefinitions.setDateRenderer(paramRenderer);
            overrideRendererDefinitions.setTimeRenderer(paramRenderer);
            return this;
        }


        public LoggingSqlConfig build() {
            if (this.loggingListeners.isEmpty()) {
                // must have at least 1 logging listener
                loggingListeners.add(DEFAULT_LOGGING_LISTENER);
            }

            this.dbType = DatabaseType.identifyDatabaseType(this.dbTypeIdentifier);

            // create initial defn's w/ defaul values
            RendererDefinitions rendereDefinitions = RendererDefinitionsFactory.createDefaultDefinitions(dbType, this.zoneId);

            // add any overrides to replace defaults (if applicable)
            mergeInOverrideDefintions(rendereDefinitions, this.overrideRendererDefinitions);
            RendererSelector rendererSelector = new RendererSelector(rendereDefinitions);

            this.tagFiller = new TagFiller(TAG, rendererSelector);

            return new LoggingSqlConfig(this);
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

}
