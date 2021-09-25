package com.github.bradjacobs.logging.jdbc.demo;

import com.github.bradjacobs.logging.jdbc.LoggingDataSource;
import com.github.bradjacobs.logging.jdbc.listeners.LoggingListener;
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

        // simple sql log listener.  will log all SQL statement at 'info' level.
        LoggingListener loggingListener = sql -> {
            if (logger.isInfoEnabled()) {
                logger.info(sql);
            }
        };

        return new LoggingDataSource(innerDataSource, loggingListener);
    }
}