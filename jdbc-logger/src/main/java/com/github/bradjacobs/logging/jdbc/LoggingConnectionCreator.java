package com.github.bradjacobs.logging.jdbc;

import com.github.bradjacobs.logging.jdbc.listeners.LoggingListener;
import com.github.bradjacobs.logging.jdbc.listeners.Slf4jLoggingListener;
import com.github.bradjacobs.logging.jdbc.param.ParamRendererFactory;
import com.github.bradjacobs.logging.jdbc.param.ParamRendererSelector;
import com.github.bradjacobs.logging.jdbc.param.ParamType;
import com.github.bradjacobs.logging.jdbc.param.SqlParamRenderer;
import com.github.bradjacobs.logging.jdbc.param.TagFiller;
import com.github.bradjacobs.logging.jdbc.param.renderer.ChronoNumericParamRenderer;
import com.github.bradjacobs.logging.jdbc.param.renderer.ChronoStringParamRenderer;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LoggingConnectionCreator makes a LoggingConnection that decorates an existing connection
 *
 * @see <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html">DateTimeFormatter</a>
 */
public class LoggingConnectionCreator
{
    private final boolean clobReaderLogging;
    private final TagFiller tagFiller;
    private final List<LoggingListener> loggingListeners;

    private static final String MISSING_LOG_LISTENER_ERR_MSG = "Logging Listeners cannot be set to null or empty collection.";

    LoggingConnectionCreator(Builder builder)
    {
        this.clobReaderLogging = builder.clobReaderLogging;
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
    public Connection create(Connection targetConnection) {
        if (targetConnection == null) {
            throw new IllegalArgumentException("Connection cannot be null.");
        }
        return new LoggingConnection(targetConnection, clobReaderLogging, tagFiller, loggingListeners);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Builder
    ///////////////////////////////////////////////////////////////////////////

    public static LoggingConnectionCreator buildDefault(LoggingListener ... logListeners) {
        return builder().withLogListener(logListeners).build();
    }

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
        private static final String TAG = "?";
        private static final ZoneId DEFAULT_ZONE = ParamRendererFactory.DEFAULT_ZONE;

        // note: force the zoneId to get set first (on constructor)
        //   otherwise possible bug when setting other fields.
        private final ZoneId zoneId;

        private DatabaseType dbType = null;
        private final List<LoggingListener> loggingListeners = new ArrayList<>();
        private boolean clobReaderLogging = Boolean.FALSE;
        private final Map<ParamType<Date>, SqlParamRenderer<Date>> datetimeOverrideMap = new HashMap<>();

        // assigned during 'build'
        private TagFiller tagFiller = null;

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

        public Builder withLogger(org.slf4j.Logger logger) {
            return withLogListener(new Slf4jLoggingListener(logger));
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
         * Enables ability to log Text Clob/Reader/InputStream parameter values
         *    WARNING: could significantly impact performance if enabled!
         * @param clobReaderLogging  (default: FALSE)
         */
        public Builder setClobReaderLogging(boolean clobReaderLogging) {
            this.clobReaderLogging = clobReaderLogging;
            return this;
        }

        /**
         * Custom date format string pattern for rendering java.sql.Timestamp as strings
         * @param pattern date format pattern
         * @see java.time.format.DateTimeFormatter
         */
        public Builder withTimestampOnlyCustomPattern(String pattern) {
            return withTimestampOnlyRenderer(new ChronoStringParamRenderer(pattern, zoneId));
        }
        protected Builder withTimestampOnlyRenderer(SqlParamRenderer<Date> renderer) {
            this.datetimeOverrideMap.put(ParamType.TIMESTAMP, renderer);
            return this;
        }

        /**
         * Custom date format string pattern for rendering java.sql.Date as strings
         * @param pattern date format pattern
         * @see java.time.format.DateTimeFormatter
         */
        public Builder withDateOnlyCustomPattern(String pattern) {
            return withDateOnlyRenderer(new ChronoStringParamRenderer(pattern, zoneId));
        }
        protected Builder withDateOnlyRenderer(SqlParamRenderer<Date> renderer) {
            this.datetimeOverrideMap.put(ParamType.DATE, renderer);
            return this;
        }

        /**
         * Custom date format string pattern for rendering java.sql.Time as strings
         * @param pattern date format pattern
         * @see java.time.format.DateTimeFormatter
         */
        public Builder withTimeOnlyCustomPattern(String pattern) {
            return withTimeOnlyRenderer(new ChronoStringParamRenderer(pattern, zoneId));
        }
        protected Builder withTimeOnlyRenderer(SqlParamRenderer<Date> renderer) {
            this.datetimeOverrideMap.put(ParamType.TIME, renderer);
            return this;
        }

        /**
         * Enable all timestamp/date/time instances to rendered as numeric (unix/epoch time)
         */
        public Builder withChronoDefaultNumerics() {
            return withChronoParamRenderer(new ChronoNumericParamRenderer());
        }

        /**
         * Apply ParamRenderer for all the timestamp/date/time types
         * @param paramRenderer paramRenderer
         */
        public Builder withChronoParamRenderer(SqlParamRenderer<Date> paramRenderer) {
            withTimestampOnlyRenderer(paramRenderer);
            withDateOnlyRenderer(paramRenderer);
            withTimeOnlyRenderer(paramRenderer);
            return this;
        }

        public LoggingConnectionCreator build() {
            if (this.loggingListeners.isEmpty()) {
                throw new IllegalStateException("Must have At least 1 loggingListener specified.");
            }

            ParamRendererSelector rendererSelector = new ParamRendererSelector(dbType, zoneId, datetimeOverrideMap);

            this.tagFiller = new TagFiller(TAG, rendererSelector);
            return new LoggingConnectionCreator(this);
        }
    }
}
