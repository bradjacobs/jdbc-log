package com.github.bradjacobs.logging.jdbc;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Calendar;

/**
 * Logging Decorator around PreparedStatements
 */
public class LoggingPreparedStatement extends LoggingStatement implements PreparedStatement
{
    // "PLACEHOLDERS" for certain parameters,
    //     b/c logging blobs and streams is typically not very useful.
    private static final String BINARY_STREAM_VALUE_PLACEHOLDER = "{_BINARYSTREAM_}";
    private static final String BLOB_VALUE_PLACEHOLDER = "{_BLOB_}";
    private static final String UNICODE_STREAM_PLACEHOLDER = "{_UNICODESTREAM_}";
    private static final String BYTES_VALUE_PLACEHOLDER = "{_BYTES_}";
    private static final String TEXT_CLOB_VALUE_PLACEHOLDER = "{_CLOB_}";

    private final PreparedStatement preparedStatement;
    private final boolean clobReaderLoggingEnabled;

    public LoggingPreparedStatement(PreparedStatement preparedStatement, LoggingConnection loggingConnection, String sql)
    {
        super(preparedStatement, loggingConnection, sql);
        this.preparedStatement = preparedStatement;
        this.clobReaderLoggingEnabled = loggingConnection.isClobReaderLoggingEnabled();
    }

    /**
     * Adds the parameter value to the tracker, which is later used to generate teh SQL string.
     * @param index parameter index
     * @param value parameter value.
     */
    protected void setCurrentParameter(int index, Object value) {
        this.sqlTracker.setParameter(index, value);
    }

    /**
     * For Reader parameters, will attempt to long the "inner string value" IFF configured
     * Otherwise will log parameter with a placeholder value.
     * @param index parameter index
     * @param reader reader
     * @return Reader
     *   if clob logging enabled  = get back a 'new' Reader to be used instead of the passed in Reader
     *   if clob logging disabled = get back the original passed in reader.
     */
    protected Reader setCurrentReaderParameter(int index, Reader reader) {
        String strValue = null;
        if (reader != null) {
            if (clobReaderLoggingEnabled) {
                strValue = extractString(reader);
                reader = new StringReader(strValue);
            }
            else {
                strValue = TEXT_CLOB_VALUE_PLACEHOLDER;
            }
        }
        setCurrentParameter(index, strValue);
        return reader;
    }

    /**
     * For TEXT InputStream parameters, will attempt to long the "inner string value" IFF configured
     * Otherwise will log parameter with a placeholder value.
     * @param index parameter index
     * @param inputStream inputStream
     * @return InputStream
     *   if clob logging enabled  = get back a 'new' InputStream to be used instead of the passed in InputStream
     *   if clob logging disabled = get back the original passed in inputStream.
     */
    protected InputStream setCurrentStreamParameter(int index, InputStream inputStream) {
        String strValue = null;
        if (inputStream != null) {
            if (clobReaderLoggingEnabled) {
                strValue = extractString(inputStream);
                inputStream = new ByteArrayInputStream(strValue.getBytes(StandardCharsets.UTF_8));
            }
            else {
                strValue = TEXT_CLOB_VALUE_PLACEHOLDER;
            }
        }
        setCurrentParameter(index, strValue);
        return inputStream;
    }

    protected void clearLogParameters() {
        sqlTracker.clearParameters();
    }


    /** @inheritDoc */
    @Override
    public void clearParameters() throws SQLException {
        clearLogParameters();
        preparedStatement.clearParameters();
    }

    /** @inheritDoc */
    @Override
    public void addBatch() throws SQLException {
        addLogBatch();
        preparedStatement.addBatch();
    }

    /** @inheritDoc */
    @Override
    public boolean execute() throws SQLException {
        logCurrent();
        return preparedStatement.execute();
    }

    /** @inheritDoc */
    @Override
    public ResultSet executeQuery() throws SQLException {
        logCurrent();
        return preparedStatement.executeQuery();
    }

    /** @inheritDoc */
    @Override
    public int executeUpdate() throws SQLException {
        logCurrent();
        return preparedStatement.executeUpdate();
    }

    /** @inheritDoc */
    @Override
    public long executeLargeUpdate() throws SQLException {
        logCurrent();
        return preparedStatement.executeLargeUpdate();
    }

    /** @inheritDoc */
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return preparedStatement.getMetaData();
    }

    /** @inheritDoc */
    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return preparedStatement.getParameterMetaData();
    }

    /** @inheritDoc */
    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setArray(parameterIndex, x);
    }

    /** @inheritDoc */
    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        x = setCurrentStreamParameter(parameterIndex, x);
        preparedStatement.setAsciiStream(parameterIndex, x);
    }

    /** @inheritDoc */
    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        x = setCurrentStreamParameter(parameterIndex, x);
        preparedStatement.setAsciiStream(parameterIndex, x, length);
    }

    /** @inheritDoc */
    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        x = setCurrentStreamParameter(parameterIndex, x);
        preparedStatement.setAsciiStream(parameterIndex, x, length);
    }

    /** @inheritDoc */
    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setBigDecimal(parameterIndex, x);
    }

    /** @inheritDoc */
    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        setCurrentParameter(parameterIndex, (x != null ? BINARY_STREAM_VALUE_PLACEHOLDER : null));
        preparedStatement.setBinaryStream(parameterIndex, x);
    }

    /** @inheritDoc */
    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setCurrentParameter(parameterIndex, (x != null ? BINARY_STREAM_VALUE_PLACEHOLDER : null));
        preparedStatement.setBinaryStream(parameterIndex, x, length);
    }

    /** @inheritDoc */
    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        setCurrentParameter(parameterIndex, (x != null ? BINARY_STREAM_VALUE_PLACEHOLDER : null));
        preparedStatement.setBinaryStream(parameterIndex, x, length);
    }

    /** @inheritDoc */
    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        setCurrentParameter(parameterIndex, (x != null ? BLOB_VALUE_PLACEHOLDER : null));
        preparedStatement.setBlob(parameterIndex, x);
    }

    /** @inheritDoc */
    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        setCurrentParameter(parameterIndex, (inputStream != null ? BLOB_VALUE_PLACEHOLDER : null));
        preparedStatement.setBlob(parameterIndex, inputStream);
    }

    /** @inheritDoc */
    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        setCurrentParameter(parameterIndex, (inputStream != null ? BLOB_VALUE_PLACEHOLDER : null));
        preparedStatement.setBlob(parameterIndex, inputStream, length);
    }

    /** @inheritDoc */
    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setBoolean(parameterIndex, x);
    }

    /** @inheritDoc */
    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setByte(parameterIndex, x);
    }

    /** @inheritDoc */
    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        setCurrentParameter(parameterIndex, (x != null ? BYTES_VALUE_PLACEHOLDER : null));
        preparedStatement.setBytes(parameterIndex, x);
    }

    /** @inheritDoc */
    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        reader = setCurrentReaderParameter(parameterIndex, reader);
        preparedStatement.setCharacterStream(parameterIndex, reader);
    }

    /** @inheritDoc */
    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        reader = setCurrentReaderParameter(parameterIndex, reader);
        preparedStatement.setCharacterStream(parameterIndex, reader, length);
    }

    /** @inheritDoc */
    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        reader = setCurrentReaderParameter(parameterIndex, reader);
        preparedStatement.setCharacterStream(parameterIndex, reader, length);
    }

    /** @inheritDoc */
    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        // note: it's possible the driver might not even allow setting a 'null' here, but still check nonetheless.
        if (x != null) {
            Reader innerReader = setCurrentReaderParameter(parameterIndex, x.getCharacterStream());
            preparedStatement.setClob(parameterIndex, innerReader);
        }
        else {
            setCurrentParameter(parameterIndex, null);
            preparedStatement.setClob(parameterIndex, x);
        }
    }

    /** @inheritDoc */
    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        reader = setCurrentReaderParameter(parameterIndex, reader);
        preparedStatement.setClob(parameterIndex, reader);
    }

    /** @inheritDoc */
    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        reader = setCurrentReaderParameter(parameterIndex, reader);
        preparedStatement.setClob(parameterIndex, reader, length);
    }

    /** @inheritDoc */
    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setDouble(parameterIndex, x);
    }

    /** @inheritDoc */
    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setDate(parameterIndex, x);
    }

    /** @inheritDoc */
    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setDate(parameterIndex, x, cal);
    }

    /** @inheritDoc */
    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setInt(parameterIndex, x);
    }

    /** @inheritDoc */
    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setFloat(parameterIndex, x);
    }

    /** @inheritDoc */
    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setLong(parameterIndex, x);
    }

    /** @inheritDoc */
    @Override
    public void setNCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        reader = setCurrentReaderParameter(parameterIndex, reader);
        preparedStatement.setNCharacterStream(parameterIndex, reader);
    }

    /** @inheritDoc */
    @Override
    public void setNClob(int parameterIndex, NClob x) throws SQLException {
        if (x != null) {
            Reader innerReader = setCurrentReaderParameter(parameterIndex, x.getCharacterStream());
            preparedStatement.setNClob(parameterIndex, innerReader);
        }
        else {
            setCurrentParameter(parameterIndex, null);
            preparedStatement.setNClob(parameterIndex, x);
        }
    }

    /** @inheritDoc */
    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        reader = setCurrentReaderParameter(parameterIndex, reader);
        preparedStatement.setNClob(parameterIndex, reader);
    }

    /** @inheritDoc */
    @Override
    public void setNCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        reader = setCurrentReaderParameter(parameterIndex, reader);
        preparedStatement.setNCharacterStream(parameterIndex, reader, length);
    }

    /** @inheritDoc */
    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        reader = setCurrentReaderParameter(parameterIndex, reader);
        preparedStatement.setNClob(parameterIndex, reader, length);
    }

    /** @inheritDoc */
    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        setCurrentParameter(parameterIndex, value);
        preparedStatement.setNString(parameterIndex, value);
    }

    /** @inheritDoc */
    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        setCurrentParameter(parameterIndex, null);
        preparedStatement.setNull(parameterIndex, sqlType);
    }

    /** @inheritDoc */
    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        setCurrentParameter(parameterIndex, null);
        preparedStatement.setNull(parameterIndex, sqlType, typeName);
    }

    /** @inheritDoc */
    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setObject(parameterIndex, x);
    }

    /** @inheritDoc */
    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setObject(parameterIndex, x, targetSqlType);
    }

    /** @inheritDoc */
    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType) throws SQLException {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setObject(parameterIndex, x, targetSqlType);
    }

    /** @inheritDoc */
    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    /** @inheritDoc */
    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    /** @inheritDoc */
    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        // NOTE: no implementation here.  (revisit IFF there's a need)
        preparedStatement.setRef(parameterIndex, x);
    }

    /** @inheritDoc */
    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        preparedStatement.setRowId(parameterIndex, x);
    }

    /** @inheritDoc */
    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setShort(parameterIndex, x);
    }

    /** @inheritDoc */
    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setString(parameterIndex, x);
    }

    /** @inheritDoc */
    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        //  Note: this is a bit of a guess b/c many drivers don't support it
        String sqlXmlString = null;
        if (xmlObject != null) {
            if (clobReaderLoggingEnabled) {
                try {
                    sqlXmlString = xmlObject.getString();
                }
                catch (Exception e) {
                    // if exception then throw a different error to show it occurred during the SQL logging process.
                    throw new SQLException("Error attempting to get string value from SQLXML for Logging: " + e.getMessage(), e);
                }
            }
            else {
                sqlXmlString = TEXT_CLOB_VALUE_PLACEHOLDER;
            }
        }
        setCurrentParameter(parameterIndex, sqlXmlString);
        preparedStatement.setSQLXML(parameterIndex, xmlObject);
    }

    /** @inheritDoc */
    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setTime(parameterIndex, x);
    }

    /** @inheritDoc */
    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setTime(parameterIndex, x, cal);
    }

    /** @inheritDoc */
    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setTimestamp(parameterIndex, x);
    }

    /** @inheritDoc */
    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setTimestamp(parameterIndex, x, cal);
    }

    /** @inheritDoc */
    @Override
    @Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setCurrentParameter(parameterIndex, UNICODE_STREAM_PLACEHOLDER);
        preparedStatement.setUnicodeStream(parameterIndex, x, length);
    }

    /** @inheritDoc */
    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        setCurrentParameter(parameterIndex, (x != null ? x.toString() : null) );
        preparedStatement.setURL(parameterIndex, x);
    }


    protected String extractString(Reader reader) {
        if (reader == null) {
            return null;
        }
        try {
            return IOUtils.toString(reader);
        }
        catch (IOException e) {
            throw new UncheckedIOException("Error attempting to get string value from reader for Logging: " + e.getMessage(), e);
        }
        finally {
            closeQuietly(reader);
        }
    }

    protected String extractString(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
        }
        catch (IOException e) {
            throw new UncheckedIOException("Error attempting to get string value from inputStream for Logging: " + e.getMessage(), e);
        }
        finally {
            closeQuietly(inputStream);
        }
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            }
            catch (IOException e) {
               /* ignore */
            }
        }
    }
}

