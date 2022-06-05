package com.github.bradjacobs.logging.jdbc;

import com.github.bradjacobs.logging.jdbc.listeners.LoggingListener;
import com.github.bradjacobs.logging.jdbc.listeners.Slf4jLoggingListener;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Builder that can create a LoggingDataSource and/or LoggingConnection
 */
// TODO ---  NEED A BETTER CLASS NAME!!
public class DbLoggingBuilder {
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");

    protected ZoneId zoneId = DEFAULT_ZONE;
    protected final List<LoggingListener> loggingListeners;
    protected boolean clobParamLogging = false;
    protected DatabaseType dbType = null;

    public static DbLoggingBuilder builder(org.slf4j.Logger logger) {
        return builder(new Slf4jLoggingListener(logger));
    }
    public static DbLoggingBuilder builder(LoggingListener ... loggingListeners) {
        return builder(Arrays.asList(loggingListeners));
    }
    public static DbLoggingBuilder builder(Collection<LoggingListener> loggingListeners) {
        // convert the collection to a list, checking if the input parameter is null
        //    or if any of the elements contains a null.
        //  (i'm sure there's a simplier way)
        List<LoggingListener> logListenerList =
            Optional.ofNullable(loggingListeners)
                    .map(Collection::stream)
                    .orElseGet(Stream::empty)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        if (logListenerList.isEmpty()) {
            throw new IllegalArgumentException("Must provide at least one logger or loggingListener");
        }
        return new DbLoggingBuilder(logListenerList);
    }

    private DbLoggingBuilder(List<LoggingListener> loggingListeners) {
        this.loggingListeners = Collections.unmodifiableList(loggingListeners);
    }

    public DbLoggingBuilder setZone(String zone) {
        return setZone(ZoneId.of(zone));
    }

    public DbLoggingBuilder setZone(ZoneId zoneId) {
        this.zoneId = (zoneId != null ? zoneId : DEFAULT_ZONE);
        return this;
    }

    public DbLoggingBuilder setClobParamLogging(boolean logClobParams) {
        this.clobParamLogging = logClobParams;
        return this;
    }

    public DbLoggingBuilder setDbType(DatabaseType dbType) {
        this.dbType = (dbType != null ? dbType : DatabaseType.UNKNOWN);
        return this;
    }

    /**
     * Creates a LoggingConnection
     * @param targetConnection the original connection to be wrapped/decorated with LoggingConnection.
     * @return loggingConnection
     */
    public LoggingConnection createFrom(Connection targetConnection) {
        if (targetConnection == null) {
            throw new IllegalArgumentException("Connection cannot be null.");
        }
        return new LoggingConnection(targetConnection, this);
    }

    /**
     * Creates a LoggingDataSource
     * @param targetDataSource the original dataSource to be wrapped/decorated with LoggingDataSource.
     * @return loggingDataSource
     */
    public LoggingDataSource createFrom(DataSource targetDataSource) {
        if (targetDataSource == null) {
            throw new IllegalArgumentException("DataSource cannot be null.");
        }
        return new LoggingDataSource(targetDataSource, this);
    }
}
