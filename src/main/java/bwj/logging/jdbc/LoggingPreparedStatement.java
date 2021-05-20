package bwj.logging.jdbc;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

public class LoggingPreparedStatement extends LoggingStatement implements PreparedStatement
{
    private final PreparedStatement preparedStatement;


    public LoggingPreparedStatement(
        PreparedStatement preparedStatement,
        List<LoggingListener> loggingListeners,
        SqlStatementTracker sqlStatementTracker)
    {
        super(preparedStatement, loggingListeners, sqlStatementTracker);
        this.preparedStatement = preparedStatement;
    }

    // some log placeholder values for certain types that will never try to actually 'log'.
    private static final String BINARY_STREAM_VALUE_PLACEHOLDER = "{_BINARYSTREAM_}";
    private static final String BLOB_VALUE_PLACEHOLDER = "{_BLOB_}";
    private static final String UNICODE_STREAM_PLACEHOLDER = "{_UNICODESTREAM_}";
    private static final String BYTES_VALUE_PLACEHOLDER = "{_BYTES_}";
    private static final String TEXT_CLOB_VALUE_PLACEHOLDER = "{_CLOB_}";



    protected void setCurrentParameter(int index, Object value) {
        this.sqlTracker.setParameter(index, value);
    }


    protected Reader setCurrentReaderParameter(int index, Reader reader) {
        if (reader == null) {
            setCurrentParameter(index, null);
        }
        else if (!sqlTracker.canLogReaderStreams()) {
            setCurrentParameter(index, TEXT_CLOB_VALUE_PLACEHOLDER);
        }
        else {
            String value = extractString(reader);
            setCurrentParameter(index, value);
            reader = new StringReader(value);
        }
        return reader;
    }

    protected InputStream setCurrentStreamParameter(int index, InputStream inputStream) {
        if (inputStream == null) {
            setCurrentParameter(index, null);
        }
        else if (!sqlTracker.canLogReaderStreams()) {
            setCurrentParameter(index, TEXT_CLOB_VALUE_PLACEHOLDER);
        }
        else {
            String value = extractString(inputStream);
            setCurrentParameter(index, value);
            inputStream = new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
        }
        return inputStream;
    }
    protected void clearLogParameters() {
        sqlTracker.clearParameters();
    }






    @Override
    public void clearParameters() throws SQLException
    {
        clearLogParameters();
        preparedStatement.clearParameters();
    }

    @Override
    public void addBatch() throws SQLException
    {
        addLogBatch();
        preparedStatement.addBatch();
    }


    @Override
    public boolean execute() throws SQLException
    {
        logCurrent();
        return preparedStatement.execute();
    }

    @Override
    public ResultSet executeQuery() throws SQLException
    {
        logCurrent();
        return preparedStatement.executeQuery();
    }

    @Override
    public int executeUpdate() throws SQLException
    {
        logCurrent();
        return preparedStatement.executeUpdate();
    }

    @Override
    public long executeLargeUpdate() throws SQLException
    {
        logCurrent();
        return preparedStatement.executeLargeUpdate();
    }


    @Override
    public ResultSetMetaData getMetaData() throws SQLException
    {
        return preparedStatement.getMetaData();
    }


    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException
    {
        return preparedStatement.getParameterMetaData();
    }


    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException
    {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setArray(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException
    {
        x = setCurrentStreamParameter(parameterIndex, x);
        preparedStatement.setAsciiStream(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException
    {
        x = setCurrentStreamParameter(parameterIndex, x);
        preparedStatement.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException
    {
        x = setCurrentStreamParameter(parameterIndex, x);
        preparedStatement.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException
    {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setBigDecimal(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException
    {
        setCurrentParameter(parameterIndex, BINARY_STREAM_VALUE_PLACEHOLDER);
        preparedStatement.setBinaryStream(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException
    {
        setCurrentParameter(parameterIndex, BINARY_STREAM_VALUE_PLACEHOLDER);
        preparedStatement.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException
    {
        setCurrentParameter(parameterIndex, BINARY_STREAM_VALUE_PLACEHOLDER);
        preparedStatement.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException
    {
        setCurrentParameter(parameterIndex, BLOB_VALUE_PLACEHOLDER);
        preparedStatement.setBlob(parameterIndex, x);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException
    {
        setCurrentParameter(parameterIndex, BLOB_VALUE_PLACEHOLDER);
        preparedStatement.setBlob(parameterIndex, inputStream);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException
    {
        setCurrentParameter(parameterIndex, BLOB_VALUE_PLACEHOLDER);
        preparedStatement.setBlob(parameterIndex, inputStream, length);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException
    {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setBoolean(parameterIndex, x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException
    {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setByte(parameterIndex, x);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException
    {
        setCurrentParameter(parameterIndex, BYTES_VALUE_PLACEHOLDER);
        preparedStatement.setBytes(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException
    {
        reader = setCurrentReaderParameter(parameterIndex, reader);
        preparedStatement.setCharacterStream(parameterIndex, reader);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException
    {
        reader = setCurrentReaderParameter(parameterIndex, reader);
        preparedStatement.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException
    {
        reader = setCurrentReaderParameter(parameterIndex, reader);
        preparedStatement.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException
    {
        if (x != null) {
            Reader innerReader = setCurrentReaderParameter(parameterIndex, x.getCharacterStream());
            preparedStatement.setClob(parameterIndex, innerReader);
        }
        else {
            setCurrentParameter(parameterIndex, null);
            preparedStatement.setClob(parameterIndex, x);
        }
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException
    {
        reader = setCurrentReaderParameter(parameterIndex, reader);
        preparedStatement.setClob(parameterIndex, reader);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException
    {
        reader = setCurrentReaderParameter(parameterIndex, reader);
        preparedStatement.setClob(parameterIndex, reader, length);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException
    {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setDouble(parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException
    {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setDate(parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException
    {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setDate(parameterIndex, x, cal);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException
    {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setInt(parameterIndex, x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException
    {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setFloat(parameterIndex, x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException
    {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setLong(parameterIndex, x);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader reader) throws SQLException
    {
        reader = setCurrentReaderParameter(parameterIndex, reader);
        preparedStatement.setNCharacterStream(parameterIndex, reader);
    }

    @Override
    public void setNClob(int parameterIndex, NClob x) throws SQLException
    {
        if (x != null) {
            Reader innerReader = setCurrentReaderParameter(parameterIndex, x.getCharacterStream());
            preparedStatement.setNClob(parameterIndex, innerReader);
        }
        else {
            setCurrentParameter(parameterIndex, null);
            preparedStatement.setNClob(parameterIndex, x);
        }
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException
    {
        reader = setCurrentReaderParameter(parameterIndex, reader);
        preparedStatement.setNClob(parameterIndex, reader);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException
    {
        reader = setCurrentReaderParameter(parameterIndex, reader);
        preparedStatement.setNCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException
    {
        reader = setCurrentReaderParameter(parameterIndex, reader);
        preparedStatement.setNClob(parameterIndex, reader, length);
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException
    {
        setCurrentParameter(parameterIndex, value);
        preparedStatement.setNString(parameterIndex, value);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException
    {
        setCurrentParameter(parameterIndex, null);
        preparedStatement.setNull(parameterIndex, sqlType);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException
    {
        setCurrentParameter(parameterIndex, null);
        preparedStatement.setNull(parameterIndex, sqlType, typeName);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException
    {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setObject(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException
    {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType) throws SQLException
    {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException
    {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException
    {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException
    {
        preparedStatement.setRef(parameterIndex, x);
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException
    {
        preparedStatement.setRowId(parameterIndex, x);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException
    {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setShort(parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException
    {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setString(parameterIndex, x);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException
    {
        setCurrentParameter(parameterIndex, xmlObject);
        preparedStatement.setSQLXML(parameterIndex, xmlObject);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException
    {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setTime(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException
    {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setTime(parameterIndex, x, cal);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException
    {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setTimestamp(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException
    {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setTimestamp(parameterIndex, x, cal);
    }

    @Override
    @Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException
    {
        setCurrentParameter(parameterIndex, UNICODE_STREAM_PLACEHOLDER);
        preparedStatement.setUnicodeStream(parameterIndex, x, length);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException
    {
        setCurrentParameter(parameterIndex, x);
        preparedStatement.setURL(parameterIndex, x);
    }





    protected String extractString(Reader reader)
    {
        if (reader == null) {
            return null;
        }
        try {
            return IOUtils.toString(reader);
        }
        catch (IOException e) {
            throw new RuntimeException("Error: " + e.getMessage(), e);
        }
        finally {
            IOUtils.closeQuietly(reader);
        }
    }
    protected String extractString(InputStream inputStream)
    {
        if (inputStream == null) {
            return null;
        }
        try {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
        }
        catch (IOException e) {
            throw new RuntimeException("Error: " + e.getMessage(), e);
        }
        finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
}

