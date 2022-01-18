package com.github.bradjacobs.logging.jdbc;

import com.github.bradjacobs.logging.jdbc.listeners.LoggingListener;
import com.github.bradjacobs.logging.jdbc.listeners.Slf4jLoggingListener;
import com.github.bradjacobs.logging.jdbc.param.ParamStringConverterFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Builder that can create a LoggingDataSource and/or LoggingConnection
 */
// TODO ---  NEED A BETTER CLASS NAME!!
public class DbLoggingBuilder
{
    protected ZoneId zoneId = ParamStringConverterFactory.DEFAULT_ZONE;
    protected final List<LoggingListener> loggingListeners = new ArrayList<>();
    protected boolean clobParamLogging = false;
    protected DatabaseType dbType = null;

    public static DbLoggingBuilder builder() {
        return new DbLoggingBuilder();
    }

    public DbLoggingBuilder()
    {
    }

    public DbLoggingBuilder setZone(String zone) {
        return setZone(ZoneId.of(zone));
    }

    public DbLoggingBuilder setZone(ZoneId zoneId) {
        this.zoneId = (zoneId != null ? zoneId : ParamStringConverterFactory.DEFAULT_ZONE);
        return this;
    }

    public DbLoggingBuilder setLogger(org.slf4j.Logger logger) {
        return setLoggingListeners(new Slf4jLoggingListener(logger));
    }
    public DbLoggingBuilder setLoggingListeners(LoggingListener loggingListener) {
        return setLoggingListeners(Collections.singleton(loggingListener));
    }
    public DbLoggingBuilder setLoggingListeners(LoggingListener ... loggingListeners) {
        if (loggingListeners != null ) {
            return setLoggingListeners(Arrays.asList(loggingListeners));
        }
        return this;
    }
    public DbLoggingBuilder setLoggingListeners(Collection<LoggingListener> loggingListeners) {
        if (loggingListeners != null && loggingListeners.size() > 0) {
            // overly cautious...don't want any 'null' values in the collection!
            this.loggingListeners.addAll( loggingListeners.stream().filter(Objects::nonNull).collect(Collectors.toList()) );
        }
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
    public LoggingConnection createFrom(Connection targetConnection)
    {
        if (targetConnection == null) {
            throw new IllegalArgumentException("Connection cannot be null.");
        }
        else if (loggingListeners.isEmpty()) {
            throw new IllegalStateException("Must provide at least one logger or loggingListener");
        }
        return new LoggingConnection(targetConnection, this);
    }

    /**
     * Creates a LoggingDataSource
     * @param targetDataSource the original dataSource to be wrapped/decorated with LoggingDataSource.
     * @return loggingDataSource
     */
    public LoggingDataSource createFrom(DataSource targetDataSource)
    {
        if (targetDataSource == null) {
            throw new IllegalArgumentException("DataSource cannot be null.");
        }
        else if (loggingListeners.isEmpty()) {
            throw new IllegalStateException("Must provide at least one logger or loggingListener");
        }
        return new LoggingDataSource(targetDataSource, this);
    }
}
