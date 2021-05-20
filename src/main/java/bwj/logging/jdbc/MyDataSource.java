package bwj.logging.jdbc;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

// todo - read this.
//    https://docs.spring.io/spring-boot/docs/1.5.14.RELEASE/reference/html/howto-data-access.html


public class MyDataSource implements DataSource
{
    private final DataSource dataSource;

    private boolean loggingConnectionEnabled = true;

    private final LoggingConnectionBuilder loggingConnectionBuilder;

    public MyDataSource(DataSource dataSource)
    {
        this(dataSource, createDefaultLoggingConnectionBuilder());
    }

    public MyDataSource(DataSource dataSource, LoggingConnectionBuilder loggingConnectionBuilder)
    {
        this.dataSource = dataSource;
        this.loggingConnectionBuilder = loggingConnectionBuilder;
    }


    private static LoggingConnectionBuilder createDefaultLoggingConnectionBuilder()
    {
        return new LoggingConnectionBuilder();
    }


    public Connection getConnection() throws SQLException
    {
        Connection innerConnection = dataSource.getConnection();
        if (!loggingConnectionEnabled) {
            return innerConnection;
        }
        return loggingConnectionBuilder.build(innerConnection);
    }

    public Connection getConnection(String username, String password) throws SQLException
    {
        Connection innerConnection = dataSource.getConnection(username, password);
        if (!loggingConnectionEnabled) {
            return innerConnection;
        }
        return loggingConnectionBuilder.build(innerConnection);
    }


    public PrintWriter getLogWriter() throws SQLException
    {
        return dataSource.getLogWriter();
    }

    public void setLogWriter(PrintWriter out) throws SQLException
    {
        dataSource.setLogWriter(out);
    }

    public void setLoginTimeout(int seconds) throws SQLException
    {
        dataSource.setLoginTimeout(seconds);
    }

    public int getLoginTimeout() throws SQLException
    {
        return dataSource.getLoginTimeout();
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        return dataSource.getParentLogger();
    }

    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        return dataSource.unwrap(iface);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return dataSource.isWrapperFor(iface);
    }

}
