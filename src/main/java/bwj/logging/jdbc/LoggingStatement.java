package bwj.logging.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Logger;
//import org.apache.log4j.Logger;

public class LoggingStatement implements Statement
{
    protected void setAndLogCurrent(String sql) {
        sqlTracker.setSql(sql);
        logCurrent();
    }
    protected void logAndClearBatch() {
        logCurrentBatch();
        sqlTracker.clearBatch();
    }
    protected void addLogBatch() {
        sqlTracker.addBatch();
    }
    protected void addLogBatch(String sql) {
        sqlTracker.addBatch(sql);
    }
    protected void clearLogBatch() {
        sqlTracker.clearBatch();
    }

    protected void logCurrent() {
        log( sqlTracker.generateSql() );
    }
    protected void logCurrentBatch() {
        String batchSql = sqlTracker.generateBatchSql();
        if (batchSql != null && batchSql.length() != 0) {
            log( batchSql );
        }
    }


    protected void log(String sql) {

        System.out.println("****  " + sql);

        if (this.loggingListeners != null) {
            for (LoggingListener loggingListener : loggingListeners) {
                loggingListener.log(sql);
            }
        }

//        if (this.loggingListener != null)
//            this.loggingListener.log(sql);
//        if (logger.isDebugEnabled())
//            logger.debug(sql);
    }




    // TODO
    //private static final Logger logger = Logger.getLogger(LoggingStatement.class);
    private static final Logger logger = null;

    private final Statement statement;
    protected final SqlStatementTracker sqlTracker;
    protected final List<LoggingListener> loggingListeners;
    protected final Connection loggingConnection;


    public LoggingStatement(Statement statement, LoggingConnection.LogStatementBuilder builder)
    {
        this.statement = statement;
        this.loggingListeners = builder.getLogListeners();
        this.sqlTracker = builder.getSqlStatementTracker();
        this.loggingConnection = builder.getConnection();
    }



    /** @inheritDoc */
    @Override
    public void addBatch(String sql) throws SQLException
    {
        addLogBatch(sql);
        statement.addBatch(sql);
    }

    /** @inheritDoc */
    @Override
    public void cancel() throws SQLException
    {
        statement.cancel();
    }

    /** @inheritDoc */
    @Override
    public void clearWarnings() throws SQLException
    {
        statement.clearWarnings();
    }

    /** @inheritDoc */
    @Override
    public void close() throws SQLException
    {
        statement.close();
    }

    /** @inheritDoc */
    @Override
    public void closeOnCompletion() throws SQLException
    {
        statement.closeOnCompletion();
    }

    /** @inheritDoc */
    @Override
    public void clearBatch() throws SQLException
    {
        clearLogBatch();
        statement.clearBatch();
    }

    /** @inheritDoc */
    @Override
    public boolean execute(String sql) throws SQLException
    {
        setAndLogCurrent(sql);
        return statement.execute(sql);
    }

    /** @inheritDoc */
    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException
    {
        setAndLogCurrent(sql);
        return statement.execute(sql, autoGeneratedKeys);
    }

    /** @inheritDoc */
    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException
    {
        setAndLogCurrent(sql);
        return statement.execute(sql, columnIndexes);
    }

    /** @inheritDoc */
    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException
    {
        setAndLogCurrent(sql);
        return statement.execute(sql, columnNames);
    }

    /** @inheritDoc */
    @Override
    public int[] executeBatch() throws SQLException
    {
        logAndClearBatch();
        return statement.executeBatch();
    }

    /** @inheritDoc */
    @Override
    public ResultSet executeQuery(String sql) throws SQLException
    {
        setAndLogCurrent(sql);
        return statement.executeQuery(sql);
    }

    /** @inheritDoc */
    @Override
    public int executeUpdate(String sql) throws SQLException
    {
        setAndLogCurrent(sql);
        return statement.executeUpdate(sql);
    }

    /** @inheritDoc */
    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException
    {
        setAndLogCurrent(sql);
        return statement.executeUpdate(sql, autoGeneratedKeys);
    }

    /** @inheritDoc */
    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException
    {
        setAndLogCurrent(sql);
        return statement.executeUpdate(sql, columnIndexes);
    }

    /** @inheritDoc */
    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException
    {
        setAndLogCurrent(sql);
        return statement.executeUpdate(sql, columnNames);
    }

    /** @inheritDoc */
    @Override
    public long[] executeLargeBatch() throws SQLException
    {
        logAndClearBatch();
        return statement.executeLargeBatch();
    }

    /** @inheritDoc */
    @Override
    public long executeLargeUpdate(String sql) throws SQLException
    {
        setAndLogCurrent(sql);
        return statement.executeLargeUpdate(sql);
    }

    /** @inheritDoc */
    @Override
    public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException
    {
        setAndLogCurrent(sql);
        return statement.executeLargeUpdate(sql, autoGeneratedKeys);
    }

    /** @inheritDoc */
    @Override
    public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException
    {
        setAndLogCurrent(sql);
        return statement.executeLargeUpdate(sql, columnIndexes);
    }

    /** @inheritDoc */
    @Override
    public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException
    {
        setAndLogCurrent(sql);
        return statement.executeLargeUpdate(sql, columnNames);
    }


    /** @inheritDoc */
    @Override
    public Connection getConnection() throws SQLException
    {
        // give the actual connection that generated this Logging Statement.
        return loggingConnection;

        //return statement.getConnection();
    }

    /** @inheritDoc */
    @Override
    public ResultSet getGeneratedKeys() throws SQLException
    {
        return statement.getGeneratedKeys();
    }

    /** @inheritDoc */
    @Override
    public int getFetchDirection() throws SQLException
    {
        return statement.getFetchDirection();
    }

    /** @inheritDoc */
    @Override
    public int getFetchSize() throws SQLException
    {
        return statement.getFetchSize();
    }

    /** @inheritDoc */
    @Override
    public long getLargeUpdateCount() throws SQLException
    {
        return statement.getLargeUpdateCount();
    }

    /** @inheritDoc */
    @Override
    public int getMaxFieldSize() throws SQLException
    {
        return statement.getMaxFieldSize();
    }

    /** @inheritDoc */
    @Override
    public void setMaxFieldSize(int max) throws SQLException
    {
        statement.setMaxFieldSize(max);
    }

    /** @inheritDoc */
    @Override
    public int getMaxRows() throws SQLException
    {
        return statement.getMaxRows();
    }

    /** @inheritDoc */
    @Override
    public boolean getMoreResults() throws SQLException
    {
        return statement.getMoreResults();
    }

    /** @inheritDoc */
    @Override
    public boolean getMoreResults(int current) throws SQLException
    {
        return statement.getMoreResults(current);
    }

    /** @inheritDoc */
    @Override
    public int getQueryTimeout() throws SQLException
    {
        return statement.getQueryTimeout();
    }

    /** @inheritDoc */
    @Override
    public ResultSet getResultSet() throws SQLException
    {
        return statement.getResultSet();
    }

    /** @inheritDoc */
    @Override
    public int getResultSetHoldability() throws SQLException
    {
        return statement.getResultSetHoldability();
    }

    /** @inheritDoc */
    @Override
    public int getResultSetConcurrency() throws SQLException
    {
        return statement.getResultSetConcurrency();
    }

    /** @inheritDoc */
    @Override
    public int getResultSetType() throws SQLException
    {
        return statement.getResultSetType();
    }

    /** @inheritDoc */
    @Override
    public int getUpdateCount() throws SQLException
    {
        return statement.getUpdateCount();
    }

    /** @inheritDoc */
    @Override
    public SQLWarning getWarnings() throws SQLException
    {
        return statement.getWarnings();
    }

    /** @inheritDoc */
    @Override
    public boolean isClosed() throws SQLException
    {
        return statement.isClosed();
    }

    /** @inheritDoc */
    @Override
    public boolean isCloseOnCompletion() throws SQLException
    {
        return statement.isCloseOnCompletion();
    }

    /** @inheritDoc */
    @Override
    public boolean isPoolable() throws SQLException
    {
        return statement.isPoolable();
    }

    /** @inheritDoc */
    @Override
    public void setCursorName(String name) throws SQLException
    {
        statement.setCursorName(name);
    }

    /** @inheritDoc */
    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException
    {
        statement.setEscapeProcessing(enable);
    }

    /** @inheritDoc */
    @Override
    public void setFetchDirection(int direction) throws SQLException
    {
        statement.setFetchDirection(direction);
    }

    /** @inheritDoc */
    @Override
    public void setFetchSize(int rows) throws SQLException
    {
        statement.setFetchSize(rows);
    }

    /** @inheritDoc */
    @Override
    public long getLargeMaxRows() throws SQLException
    {
        return statement.getLargeMaxRows();
    }

    /** @inheritDoc */
    @Override
    public void setLargeMaxRows(long max) throws SQLException
    {
        statement.setLargeMaxRows(max);
    }

    /** @inheritDoc */
    @Override
    public void setMaxRows(int max) throws SQLException
    {
        statement.setMaxRows(max);
    }

    /** @inheritDoc */
    @Override
    public void setPoolable(boolean poolable) throws SQLException
    {
        statement.setPoolable(poolable);
    }

    /** @inheritDoc */
    @Override
    public void setQueryTimeout(int seconds) throws SQLException
    {
        statement.setQueryTimeout(seconds);
    }

    /** @inheritDoc */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        return statement.unwrap(iface);
    }

    /** @inheritDoc */
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return statement.isWrapperFor(iface);
    }
}
