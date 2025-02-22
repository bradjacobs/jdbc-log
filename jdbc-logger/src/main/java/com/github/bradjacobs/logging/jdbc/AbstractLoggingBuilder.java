package com.github.bradjacobs.logging.jdbc;

import com.github.bradjacobs.logging.jdbc.listeners.LoggingListener;
import com.github.bradjacobs.logging.jdbc.listeners.Slf4jLoggingListener;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.bradjacobs.logging.jdbc.DatabaseType.*;

/**
 * Builder that can create a LoggingDataSource and/or LoggingConnection
 */
// TODO ---  NEED A BETTER CLASS NAME!!
abstract public class AbstractLoggingBuilder<T extends AbstractLoggingBuilder<T>> {
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");

    protected ZoneId zoneId = DEFAULT_ZONE;
    protected List<LoggingListener> loggingListeners = new ArrayList<>();
    protected boolean clobParamLogging = false;
    protected DatabaseType dbType = DEFAULT;

    public AbstractLoggingBuilder() { }

    public T logger(org.slf4j.Logger logger) {
        return loggingListener(new Slf4jLoggingListener(logger));
    }
    public T loggingListener(LoggingListener loggingListener) {
        return loggingListeners(loggingListener);
    }
    public T loggingListeners(LoggingListener ... loggingListeners) {
        return loggingListeners(Arrays.asList(loggingListeners));
    }
    public T loggingListeners(Collection<LoggingListener> loggingListeners) {
        // convert the collection to a list, checking if the input parameter is null
        //    or if any of the elements contains a null.
        //  (i'm sure there's a simpler way)
        List<LoggingListener> logListenerList =
                Optional.ofNullable(loggingListeners)
                        .map(Collection::stream)
                        .orElseGet(Stream::empty)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        if (logListenerList.isEmpty()) {
            throw new IllegalArgumentException("Must provide at least one logger or loggingListener");
        }
        this.loggingListeners.addAll(logListenerList);
        return self();
    }

    public T zone(String zone) {
        return zoneId(ZoneId.of(zone));
    }

    public T zoneId(ZoneId zoneId) {
        this.zoneId = (zoneId != null ? zoneId : DEFAULT_ZONE);
        return self();
    }

    public T clobParamLogging(boolean logClobParams) {
        this.clobParamLogging = logClobParams;
        return self();
    }

    public T dbType(DatabaseType dbType) {
        this.dbType = (dbType != null ? dbType : DEFAULT);
        return self();
    }

    abstract protected T self();
}
