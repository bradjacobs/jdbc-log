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
    private DataSource dataSource;
    private final LoggingListener loggingListener = null;

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
        LoggingConnectionBuilder loggingConnectionBuilder = new LoggingConnectionBuilder();
        loggingConnectionBuilder.setLogTextStreams(false);
        return loggingConnectionBuilder;
    }


    public Connection getConnection() throws SQLException
    {
        Connection internalConnection = dataSource.getConnection();
        return loggingConnectionBuilder.build(internalConnection);
    }

    public Connection getConnection(String username, String password) throws SQLException
    {
        Connection internalConnection = dataSource.getConnection(username, password);
        return loggingConnectionBuilder.build(internalConnection);
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
