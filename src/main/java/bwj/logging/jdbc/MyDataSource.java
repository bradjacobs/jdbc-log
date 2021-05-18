package bwj.logging.jdbc;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

// todo - read this.
//    https://docs.spring.io/spring-boot/docs/1.5.14.RELEASE/reference/html/howto-data-access.html

//  https://www.baeldung.com/hibernate-persist-json-object


public class MyDataSource implements DataSource
{
    private DataSource dataSource;
    private final LoggingListener loggingListener = null;


    public MyDataSource(DataSource dataSource)
    {

        this.dataSource = dataSource;
    }

    public Connection getConnection() throws SQLException
    {
        try {
            return new LoggingConnection(dataSource.getConnection(), loggingListener);
        }
        catch (Exception e) {
            e.printStackTrace();;
            System.out.println("ERROR: " + e.getMessage());
            int kjkj= 33;
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection(String username, String password) throws SQLException
    {
        try
        {
            return new LoggingConnection(dataSource.getConnection(username, password), loggingListener);
        }
        catch (Exception e) {
            e.printStackTrace();;
            System.out.println("ERROR: " + e.getMessage());
            int kjkj= 33;
            throw new RuntimeException(e);
        }

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
