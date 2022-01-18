package com.github.bradjacobs.logging.jdbc;

import com.github.bradjacobs.logging.jdbc.listeners.LoggingListener;
import com.github.bradjacobs.logging.jdbc.listeners.Slf4jLoggingListener;
import com.github.bradjacobs.logging.jdbc.param.DefaultParamToStringConverter;
import com.github.bradjacobs.logging.jdbc.param.ParamStringConverterFactory;
import com.github.bradjacobs.logging.jdbc.param.ParamToStringConverter;
import com.github.bradjacobs.logging.jdbc.param.SqlTagFiller;

import java.sql.Connection;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * LoggingConnectionCreator makes a LoggingConnection that decorates an existing connection
 *
 * @see <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html">DateTimeFormatter</a>
 */

//  TODO -- this class will probably be removed after future refactors!!
//
public class LoggingConnectionCreator
{
    private final boolean clobReaderLogging;
    private final SqlTagFiller sqlTagFiller;
    private final List<LoggingListener> loggingListeners;

    private static final String MISSING_LOG_LISTENER_ERR_MSG = "Logging Listeners cannot be set to null or empty collection.";

    LoggingConnectionCreator(Builder builder)
    {
        this.clobReaderLogging = builder.clobReaderLogging;
        this.sqlTagFiller = new SqlTagFiller(builder.paramToStringConverter);
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
        return new LoggingConnection(targetConnection, clobReaderLogging, sqlTagFiller, loggingListeners);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Builder
    ///////////////////////////////////////////////////////////////////////////

    public static LoggingConnectionCreator buildDefault(LoggingListener ... logListeners) {
        return builder().withLogListener(logListeners).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder
    {
        private DatabaseType dbType = null;
        private ZoneId zoneId = null;
        private final List<LoggingListener> loggingListeners = new ArrayList<>();
        private boolean clobReaderLogging = Boolean.FALSE;
        private ParamToStringConverter paramToStringConverter = null;

        /**
         * LoggingSqlConfig.Builder constructor
         */
        protected Builder() { }

        public Builder withLogger(org.slf4j.Logger logger) {
            return withLogListener(new Slf4jLoggingListener(logger));
        }

        /**
         * Any string value that can help identify which type of database is used.  (MySQL, HSQL, PostGres, etc.)
         *   Could be JDBC url, driver class name, hardcoded string.
         * The config will look for specific 'substrings' to determine database type.
         *   (i.e.  if contains 'mysql', must be a MySQL database)
         * @param dbTypeIdentifier string identifying database used.  If not set then 'general database defaults' will be selected.
         * @deprecated
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
         * Set explicit timeZone for use when generating DateTime strings
         * @param timeZone timeZone string
         */
        public Builder withTimeZone(String timeZone) {
            return withTimeZone(ZoneId.of(timeZone));
        }

        /**
         * Set explicit timeZone for use when generating DateTime strings
         * @param zoneId the ZoneId
         */
        public Builder withTimeZone(ZoneId zoneId) {
            this.zoneId = zoneId;
            return this;
        }

        public LoggingConnectionCreator build() {
            if (this.loggingListeners.isEmpty()) {
                throw new IllegalStateException("Must have At least 1 loggingListener specified.");
            }

            // todo - fix - this is interim solution
            this.paramToStringConverter = ParamStringConverterFactory.getParamConverter(this.dbType, this.zoneId);
            return new LoggingConnectionCreator(this);
        }
    }
}
