package com.github.bradjacobs.logging.jdbc.hsql;

import com.github.bradjacobs.logging.jdbc.DbLoggingBuilder;
import com.github.bradjacobs.logging.jdbc.hsql.objects.BloatedPojo;
import com.github.bradjacobs.logging.jdbc.hsql.objects.CaptureLoggingListener;
import com.github.bradjacobs.logging.jdbc.hsql.objects.PojoDAO;
import com.github.bradjacobs.logging.jdbc.listeners.SystemOutLogListener;
import org.apache.commons.io.IOUtils;
import org.hsqldb.jdbc.JDBCClob;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;


abstract public class AbstractPojoLoggingTest
{
    protected PojoDAO initializePojoDao(CaptureLoggingListener captureLoggingListener, boolean clobLoggingEnabled) throws Exception
    {
        Connection innerConn = DriverManager.getConnection("jdbc:hsqldb:mem:sampleDB", "SA", "");

        DbLoggingBuilder dbLoggingBuilder =
                DbLoggingBuilder.builder()
                        .setLoggingListeners(captureLoggingListener, new SystemOutLogListener())
                        .setClobParamLogging(clobLoggingEnabled);

        Connection dbcon = dbLoggingBuilder.createFrom(innerConn);
        PojoDAO pojoDao = new PojoDAO(dbcon);
        pojoDao.init();
        return pojoDao;
    }


    protected BloatedPojo createTestPojo(Integer id, String name, int intValue, double doubleValue,
                                         Long sqlDate, Long sqlTimestamp, String clobString, String inputStreamString)
    {
        BloatedPojo pojo = new BloatedPojo();
        if (id != null) {
            pojo.setId(id);
        }
        pojo.setName(name);
        pojo.setIntValue(intValue);
        pojo.setDoubleValue(doubleValue);

        if (sqlDate != null) {
            pojo.setSqlDateValue(new java.sql.Date(sqlDate));
        }
        if (sqlTimestamp != null) {
            pojo.setSqlTimestampValue(new java.sql.Timestamp(sqlTimestamp));
        }

        if (clobString != null) {
            pojo.setClobValue( createClobValue(clobString) );
        }
        if (inputStreamString != null) {
            pojo.setStreamValue( createInputStreamValue(inputStreamString) );
        }

        return pojo;
    }

    protected BloatedPojo createDummyPojo(String name) {
        return createTestPojo(
                null,
                name,
                30,
                0d,
                1538014031000L,
                1538014031000L,
                "MY__TEST__CLOB",
                null);
    }



    protected void assertPojoEqual(BloatedPojo actualPojo, BloatedPojo expectedPojo)
    {
        if (expectedPojo == null) {
            assertNull(actualPojo);
            return;
        }
        assertNotNull(actualPojo);

        if (expectedPojo.getId() != 0) {
            assertEquals(actualPojo.getId(), expectedPojo.getId());
        }
        assertEquals(actualPojo.getName(), expectedPojo.getName());
        assertEquals(actualPojo.getIntValue(), expectedPojo.getIntValue());
        assertEquals(actualPojo.getDoubleValue(), expectedPojo.getDoubleValue(), 0.0001d);

        assertEquals(String.valueOf(actualPojo.getSqlDateValue()), String.valueOf(expectedPojo.getSqlDateValue()));

        assertEquals(actualPojo.getSqlTimestampValue(), expectedPojo.getSqlTimestampValue());

        // note: converting to string will 'mess up' the underlying stream, but don't care in this case.
        Clob actualClob = actualPojo.getClobValue();
        Clob expectedClob = expectedPojo.getClobValue();
        if (actualClob != null) {
            assertNotNull(expectedClob);
            assertEquals( extractString(actualClob), extractString(expectedClob) );
        }
        else {
            assertNull(expectedClob);
        }

        //    NOTE: can't test this b/c after calling the dao b/c 'inputStream' can no longer be read for a value (expected)
        //InputStream actualStreamValue = actualPojo.getStreamValue();
        //InputStream expectedStreamValue = expectedPojo.getStreamValue();
        //if (actualStreamValue != null) {
        //    assertNotNull(expectedStreamValue);
        //    assertEquals( extractString(actualStreamValue), extractString(expectedStreamValue) );
        //}
        //else {
        //    assertNull(expectedStreamValue);
        //}
    }

    private InputStream createInputStreamValue(String value)
    {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }

    private Clob createClobValue(String value)
    {
        try {
            return new JDBCClob(value);
        }
        catch (SQLException e) {
            throw new RuntimeException("Unable create clob: " + e.getMessage(), e);
        }
    }


    protected String extractString(Clob clob)
    {
        if (clob == null) {
            return null;
        }
        Reader reader = null;
        try {
            reader = clob.getCharacterStream();
            return IOUtils.toString(reader);
        }
        catch (Exception e) {
            throw new RuntimeException("Error attempting to get string value from reader for Logging: " + e.getMessage(), e);
        }
        finally {
            closeQuietly(reader);
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
            throw new UncheckedIOException("Error attempting to get string value from inputStream for Logging: " + e.getMessage(), e);
        }
        finally {
            closeQuietly(inputStream);
        }
    }


    private void closeQuietly(Closeable closeable)
    {
        if (closeable != null)
        {
            try {
                closeable.close();
            }
            catch (IOException e) {
                /* ignore */
            }
        }
    }
}
