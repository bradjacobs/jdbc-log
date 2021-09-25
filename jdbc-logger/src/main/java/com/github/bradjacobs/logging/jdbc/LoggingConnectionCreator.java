package com.github.bradjacobs.logging.jdbc;

import com.github.bradjacobs.logging.jdbc.listeners.LoggingListener;
import com.github.bradjacobs.logging.jdbc.listeners.SystemOutLogListener;
import com.github.bradjacobs.logging.jdbc.param.ParamRendererSelector;
import com.github.bradjacobs.logging.jdbc.param.RendererDefinitions;
import com.github.bradjacobs.logging.jdbc.param.RendererDefinitionsFactory;
import com.github.bradjacobs.logging.jdbc.param.SqlParamRenderer;
import com.github.bradjacobs.logging.jdbc.param.SqlParamRendererGenerator;
import com.github.bradjacobs.logging.jdbc.param.TagFiller;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * LoggingConnectionCreator makes a LoggingConnection that decorates an existing connection
 *
 * @see <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html">DateTimeFormatter</a>
 */
public class LoggingConnectionCreator
{
    private boolean loggingEnabled = true;  // flag for enabling/disabling

    private final boolean streamLogging;
    private final TagFiller tagFiller;
    private final List<LoggingListener> loggingListeners;

    private static final String MISSING_LOG_LISTENER_ERR_MSG = "Logging Listeners cannot be set to null or empty collection.";

    LoggingConnectionCreator(Builder builder)
    {
        this.streamLogging = builder.streamLogging;
        this.tagFiller = builder.tagFiller;
        this.loggingListeners = Collections.unmodifiableList(builder.loggingListeners);
    }

    public List<LoggingListener> getLoggingListeners()
    {
        return loggingListeners;
    }

    /**
     * Creates a LoggingConnection
     * @param targetConnection the original connection to be wrapped/decorated with LoggingConnection.
     * @return loggingConnection
     */
    public Connection getConnection(Connection targetConnection) {
        if (targetConnection == null) {
            throw new IllegalArgumentException("Connection cannot be null.");
        }

        if (this.loggingEnabled) {
            return new LoggingConnection(targetConnection, streamLogging, tagFiller, loggingListeners);
        }
        else {
            return targetConnection;
        }
    }


    /**
     * If true, getConnection will return a "LoggingConnection",
     * If false, getConnection will pass back the original connection given.
     * @return true if sql Logging enabled
     */
    public boolean isLoggingEnabled()
    {
        return loggingEnabled;
    }

    /**
     * Enable/Disable logging
     * @param loggingEnabled loggingEnabled
     */
    public void setLoggingEnabled(boolean loggingEnabled)
    {
        this.loggingEnabled = loggingEnabled;
    }



    ///////////////////////////////////////////////////////////////////////////
    // Builder
    ///////////////////////////////////////////////////////////////////////////

    public static Builder builder() {
        return new Builder(null);  // this will be set to 'default' timezone
    }
    public static Builder builder(String timeZone) {
        if (StringUtils.isEmpty(timeZone)) {
            return builder();
        }
        return builder(ZoneId.of(timeZone));
    }
    public static Builder builder(ZoneId zoneId) {
        return new Builder(zoneId);
    }


    public static class Builder
    {
        private static final SqlParamRendererGenerator paramRendererGenerator = new SqlParamRendererGenerator();

        private static final String TAG = "?";
        private static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");
        private static final LoggingListener DEFAULT_LOGGING_LISTENER = new SystemOutLogListener(); // used only if no listeners are provided.

        // note: force the zoneId to get set first (on constructor)
        //   otherwise possible bug when setting other fields.
        private final ZoneId zoneId;

        private DatabaseType dbType = null;
        private final List<LoggingListener> loggingListeners = new ArrayList<>();
        private final RendererDefinitions overrideRendererDefinitions = new RendererDefinitions();
        private boolean streamLogging = Boolean.FALSE;

        // assigned during 'build'
        private TagFiller tagFiller;


        /**
         * LoggingSqlConfig.Builder constructor
         *   Can optionally supply a custom timezone (used any Date/Timestamp string value formatting.)
         *     (note: 'ZoneId.systemDefault' can be used for the current local timezone
         * @param zoneId timeZone  (default: UTC)
         */
        protected Builder(ZoneId zoneId) {
            if (zoneId == null) {
                zoneId = DEFAULT_ZONE;
            }
            this.zoneId = zoneId;
        }


        /**
         * Any string value that can help identify which type of database is used.  (MySQL, HSQL, PostGres, etc.)
         *   Could be JDBC url, driver class name, hardcoded string.
         * The config will look for specific 'substrings' to determine database type.
         *   (i.e.  if contains 'mysql', must be a MySQL database)
         * @param dbTypeIdentifier string identifying database used.  If not set then 'general database defaults' will be selected.
         */
        public Builder withDbTypeIdentifier(String dbTypeIdentifier) {
            this.dbType = DatabaseType.identifyDatabaseType(dbTypeIdentifier);
            return this;
        }


        public Builder withLogListener(LoggingListener ... logListeners) {
            if (logListeners == null || logListeners.length == 0) {
                throw new IllegalArgumentException(MISSING_LOG_LISTENER_ERR_MSG);
            }
            return withLogListener(Arrays.asList(logListeners));
        }

        public Builder withLogListener(Collection<LoggingListener> logListeners) {
            if (logListeners == null || logListeners.size() == 0 || logListeners.contains(null)) {
                throw new IllegalArgumentException(MISSING_LOG_LISTENER_ERR_MSG);
            }
            loggingListeners.addAll(logListeners);
            return this;
        }

        /**
         * Enables ability to log Text Clob/Reader/InputStream values
         *    WARNING: could significantly impact performance if enabled!
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
         * Enable all timestamp/date/time instances to rendered as numeric (unix/epoch time)
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


        public LoggingConnectionCreator build() {
            if (this.loggingListeners.isEmpty()) {
                loggingListeners.add(DEFAULT_LOGGING_LISTENER);  // must have at least 1 listener
            }

            // create initial definitions w/ default values
            RendererDefinitions rendererDefinitions = RendererDefinitionsFactory.createDefaultDefinitions(dbType, this.zoneId);

            // add any overrides to replace defaults (if applicable)
            rendererDefinitions.mergeIn(this.overrideRendererDefinitions);
            ParamRendererSelector rendererSelector = new ParamRendererSelector(rendererDefinitions);

            this.tagFiller = new TagFiller(TAG, rendererSelector);
            return new LoggingConnectionCreator(this);
        }
    }

}
