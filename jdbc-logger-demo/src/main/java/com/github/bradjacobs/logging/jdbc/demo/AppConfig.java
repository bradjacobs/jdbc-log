package com.github.bradjacobs.logging.jdbc.demo;

import com.github.bradjacobs.logging.jdbc.LoggingDataSource;
import com.github.bradjacobs.logging.jdbc.listeners.LoggingListener;
import com.github.bradjacobs.logging.jdbc.listeners.Slf4jLoggingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class AppConfig
{
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    @Autowired
    private DataSourceProperties dataSourceProperties;

    @Value("${custom.logging.enabled}")
    private Boolean customLoggingEnabled;

    public AppConfig() { }

    @Primary
    @Bean
    @ConfigurationProperties(prefix = "datasource")
    public DataSource dataSource()
    {
        DataSource innerDataSource = dataSourceProperties.initializeDataSourceBuilder().build();
        if (! Boolean.TRUE.equals(customLoggingEnabled)) {
            return innerDataSource;
        }

        // example 1
        //   'custom' sql log listener.  will log all SQL statement at 'info' level.
        LoggingListener loggingListenerInfo = sql -> {
            if (logger.isInfoEnabled()) {
                logger.info(sql);
            }
        };

        // example 2
        //   pre-built default logListener for slf4j
        LoggingListener loggingListener = new Slf4jLoggingListener(logger);


        // extra convenience constructor to pass in logger directly  (behaves as example 2)
        return new LoggingDataSource(innerDataSource, logger);


        //return new LoggingDataSource(innerDataSource, loggingListener);
    }
}