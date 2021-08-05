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
        int kkkk = 33;
        int kls =- 33;
    }

//    @Bean
//    @Primary
//    @ConfigurationProperties(prefix = "datasource")
//    public DataSource dataSource() {
//        DataSource innerDs = DataSourceBuilder.create().build();
//        return new MyDataSource(innerDs);
//    }


    @Primary
    @Bean
    @ConfigurationProperties(prefix = "datasource")
    public DataSource dataSource() {

        DataSource innerDataSource = dataSourceProperties.initializeDataSourceBuilder().build();


        try
        {
            Connection c = innerDataSource.getConnection();
            int kj = 3333;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }


        LoggingListener loggingListener = sql -> {
            if (logger.isInfoEnabled()) {
                logger.info(sql);
            }
        };

//        return innerDataSource;

        LoggingConnectionCreator logConnCreator = LoggingConnectionCreator.builder().withLogListener(loggingListener).build();


        LoggingDataSource loggingDataSource = new LoggingDataSource(innerDataSource, loggingListener);

        return loggingDataSource;
    }
}