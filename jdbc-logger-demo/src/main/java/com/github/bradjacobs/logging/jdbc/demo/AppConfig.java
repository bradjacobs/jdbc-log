package com.github.bradjacobs.logging.jdbc.demo;

import com.github.bradjacobs.logging.jdbc.LoggingConnectionCreator;
import com.github.bradjacobs.logging.jdbc.LoggingDataSource;
import com.github.bradjacobs.logging.jdbc.LoggingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Configuration
public class AppConfig
{
    @Autowired
    private DataSourceProperties dataSourceProperties;

    Logger logger = LoggerFactory.getLogger(AppConfig.class);

    public AppConfig()
    {
    }



    @Primary
    @Bean
    @ConfigurationProperties(prefix = "datasource")
    public DataSource dataSource() {

        DataSource innerDataSource = dataSourceProperties.initializeDataSourceBuilder().build();

        // below lead to a way to runtime detect the actual db type (based on driver)
        //   but currently very low priority
//        try {
//            Connection c = innerDataSource.getConnection();
//        }
//        catch (SQLException e) {
//            e.printStackTrace();
//        }


        LoggingListener loggingListener = sql -> {
            if (logger.isInfoEnabled()) {
                logger.info(sql);
            }
        };

        LoggingConnectionCreator logConnCreator = LoggingConnectionCreator.builder().withLogListener(loggingListener).build();

        LoggingDataSource loggingDataSource = new LoggingDataSource(innerDataSource, loggingListener);

        return loggingDataSource;
    }
}