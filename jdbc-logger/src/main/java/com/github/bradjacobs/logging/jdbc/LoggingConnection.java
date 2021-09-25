package com.github.bradjacobs.logging.jdbc;

import com.github.bradjacobs.logging.jdbc.listeners.LoggingListener;
import com.github.bradjacobs.logging.jdbc.param.TagFiller;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class LoggingConnection implements Connection
{
    private final Connection targetConnection;

    protected final boolean isStreamLoggingEnabled;
    protected final List<LoggingListener> loggingListeners;
    protected final TagFiller tagFiller;


    /**
     * Logging Connection constructor.
     *   NOTE: Please Use LoggingConnectionCreator to make instance
     * @param targetConnection original jdbc targetConnection
     */
    LoggingConnection(Connection targetConnection, boolean streamLogging, TagFiller tagFiller, List<LoggingListener> loggingListeners)
    {
        if (targetConnection == null) {
            throw new IllegalArgumentException("Must provide a targetConnection");
        }

        this.targetConnection = targetConnection;
        this.isStreamLoggingEnabled = streamLogging;
        this.tagFiller = tagFiller;
        this.loggingListeners = Collections.unmodifiableList(loggingListeners);
    }


    private Statement logWrap(Statement statement)
    {
        return new LoggingStatement(statement, this);
    }
    private PreparedStatement logWrap(PreparedStatement preparedStatement, String sql)
    {
        return new LoggingPreparedStatement(preparedStatement, this, sql);
    }
    private CallableStatement logWrap(CallableStatement callableStatement, String sql)
    {
        return new LoggingCallableStatement(callableStatement, this, sql);
    }




    /** @inheritDoc */
    @Override
    public Statement createStatement() throws SQLException
    {
        return logWrap(targetConnection.createStatement());
    }

    /** @inheritDoc */
    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
    {
        return logWrap(targetConnection.createStatement(resultSetType, resultSetConcurrency));
    }

    /** @inheritDoc */
    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
    {
        return logWrap(targetConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
    }


    /** @inheritDoc */
    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException
    {
        return logWrap(targetConnection.prepareStatement(sql), sql);
    }

    /** @inheritDoc */
    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
    {
        return logWrap(targetConnection.prepareStatement(sql, autoGeneratedKeys), sql);
    }

    /** @inheritDoc */
    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException
    {
        return logWrap(targetConnection.prepareStatement(sql, columnIndexes), sql);
    }

    /** @inheritDoc */
    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
    {
        return logWrap(targetConnection.prepareStatement(sql, columnNames), sql);
    }

    /** @inheritDoc */
    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
    {
        return logWrap(targetConnection.prepareStatement(sql, resultSetType, resultSetConcurrency), sql);
    }

    /** @inheritDoc */
    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
    {
        return logWrap(targetConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability), sql);
    }


    /** @inheritDoc */
    @Override
    public CallableStatement prepareCall(String sql) throws SQLException
    {
        return logWrap(targetConnection.prepareCall(sql), sql);
    }

    /** @inheritDoc */
    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
    {
        return logWrap(targetConnection.prepareCall(sql, resultSetType, resultSetConcurrency), sql);
    }

    /** @inheritDoc */
    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
    {
        return logWrap(targetConnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability), sql);
    }


    /** @inheritDoc */
    @Override
    public String nativeSQL(String sql) throws SQLException
    {
        return targetConnection.nativeSQL(sql);
    }


    /** @inheritDoc */
    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException
    {
        targetConnection.setAutoCommit(autoCommit);
    }

    /** @inheritDoc */
    @Override
    public boolean getAutoCommit() throws SQLException
    {
        return targetConnection.getAutoCommit();
    }

    /** @inheritDoc */
    @Override
    public void commit() throws SQLException
    {
        targetConnection.commit();
    }

    /** @inheritDoc */
    @Override
    public void rollback() throws SQLException
    {
        targetConnection.rollback();
    }

    /** @inheritDoc */
    @Override
    public void close() throws SQLException
    {
        targetConnection.close();
    }

    /** @inheritDoc */
    @Override
    public boolean isClosed() throws SQLException
    {
        return targetConnection.isClosed();
    }

    /** @inheritDoc */
    @Override
    public DatabaseMetaData getMetaData() throws SQLException
    {
        return targetConnection.getMetaData();
    }

    /** @inheritDoc */
    @Override
    public void setReadOnly(boolean readOnly) throws SQLException
    {
        targetConnection.setReadOnly(readOnly);
    }

    /** @inheritDoc */
    @Override
    public boolean isReadOnly() throws SQLException
    {
        return targetConnection.isReadOnly();
    }

    /** @inheritDoc */
    @Override
    public void setCatalog(String catalog) throws SQLException
    {
        targetConnection.setCatalog(catalog);
    }

    /** @inheritDoc */
    @Override
    public String getCatalog() throws SQLException
    {
        return targetConnection.getCatalog();
    }

    /** @inheritDoc */
    @Override
    public void setTransactionIsolation(int level) throws SQLException
    {
        targetConnection.setTransactionIsolation(level);
    }

    /** @inheritDoc */
    @Override
    public int getTransactionIsolation() throws SQLException
    {
        return targetConnection.getTransactionIsolation();
    }

    /** @inheritDoc */
    @Override
    public SQLWarning getWarnings() throws SQLException
    {
        return targetConnection.getWarnings();
    }

    /** @inheritDoc */
    @Override
    public void clearWarnings() throws SQLException
    {
        targetConnection.clearWarnings();
    }

    /** @inheritDoc */
    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException
    {
        return targetConnection.getTypeMap();
    }

    /** @inheritDoc */
    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException
    {
        targetConnection.setTypeMap(map);
    }

    /** @inheritDoc */
    @Override
    public void setHoldability(int holdability) throws SQLException
    {
        targetConnection.setHoldability(holdability);
    }

    /** @inheritDoc */
    @Override
    public int getHoldability() throws SQLException
    {
        return targetConnection.getHoldability();
    }

    /** @inheritDoc */
    @Override
    public Savepoint setSavepoint() throws SQLException
    {
        return targetConnection.setSavepoint();
    }

    /** @inheritDoc */
    @Override
    public Savepoint setSavepoint(String name) throws SQLException
    {
        return targetConnection.setSavepoint(name);
    }

    /** @inheritDoc */
    @Override
    public void rollback(Savepoint savepoint) throws SQLException
    {
        targetConnection.rollback(savepoint);
    }

    /** @inheritDoc */
    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException
    {
        targetConnection.releaseSavepoint(savepoint);
    }


    /** @inheritDoc */
    @Override
    public Clob createClob() throws SQLException
    {
        return targetConnection.createClob();
    }

    /** @inheritDoc */
    @Override
    public Blob createBlob() throws SQLException
    {
        return targetConnection.createBlob();
    }

    /** @inheritDoc */
    @Override
    public NClob createNClob() throws SQLException
    {
        return targetConnection.createNClob();
    }

    /** @inheritDoc */
    @Override
    public SQLXML createSQLXML() throws SQLException
    {
        return targetConnection.createSQLXML();
    }

    /** @inheritDoc */
    @Override
    public boolean isValid(int timeout) throws SQLException
    {
        return targetConnection.isValid(timeout);
    }

    /** @inheritDoc */
    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException
    {
        targetConnection.setClientInfo(name, value);
    }

    /** @inheritDoc */
    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException
    {
        targetConnection.setClientInfo(properties);
    }

    /** @inheritDoc */
    @Override
    public String getClientInfo(String name) throws SQLException
    {
        return targetConnection.getClientInfo(name);
    }

    /** @inheritDoc */
    @Override
    public Properties getClientInfo() throws SQLException
    {
        return targetConnection.getClientInfo();
    }

    /** @inheritDoc */
    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException
    {
        return targetConnection.createArrayOf(typeName, elements);
    }

    /** @inheritDoc */
    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException
    {
        return targetConnection.createStruct(typeName, attributes);
    }

    /** @inheritDoc */
    @Override
    public void setSchema(String schema) throws SQLException
    {
        targetConnection.setSchema(schema);
    }

    /** @inheritDoc */
    @Override
    public String getSchema() throws SQLException
    {
        return targetConnection.getSchema();
    }

    /** @inheritDoc */
    @Override
    public void abort(Executor executor) throws SQLException
    {
        targetConnection.abort(executor);
    }

    /** @inheritDoc */
    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException
    {
        targetConnection.setNetworkTimeout(executor, milliseconds);
    }

    /** @inheritDoc */
    @Override
    public int getNetworkTimeout() throws SQLException
    {
        return targetConnection.getNetworkTimeout();
    }

    /** @inheritDoc */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        return targetConnection.unwrap(iface);
    }

    /** @inheritDoc */
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return targetConnection.isWrapperFor(iface);
    }

}
