package com.github.bradjacobs.logging.jdbc.hsql;

import com.github.bradjacobs.logging.jdbc.LoggingConnection;
import com.github.bradjacobs.logging.jdbc.hsql.objects.BloatedPojo;
import com.github.bradjacobs.logging.jdbc.hsql.objects.CaptureLoggingListener;
import com.github.bradjacobs.logging.jdbc.hsql.objects.PojoDAO;
import com.github.bradjacobs.logging.jdbc.listeners.SystemOutLogListener;
import org.apache.commons.io.IOUtils;

import java.io.Reader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

abstract public class AbstractPojoLoggingTest {
    protected PojoDAO initializePojoDao(CaptureLoggingListener captureLoggingListener, boolean clobLoggingEnabled) throws Exception {
        Connection innerConn = DriverManager.getConnection("jdbc:hsqldb:mem:sampleDB", "SA", "");

        Connection dbcon = LoggingConnection
                .builder(innerConn)
                .loggingListeners(captureLoggingListener, new SystemOutLogListener())
                .clobParamLogging(clobLoggingEnabled)
                .build();

        PojoDAO pojoDao = new PojoDAO(dbcon);
        pojoDao.init();
        return pojoDao;
    }

    protected BloatedPojo createDummyPojo(String name) {
        return BloatedPojo.builder()
                .id(null).name(name).invValue(30).doubleValue(0d)
                .sqlDate(1538014031000L).sqlTimestamp(1538014031000L)
                .clobString("MY__TEST__CLOB").inputStreamString(null)
                .build();
    }

    /**
     * Special assert method to get information on exactly which field
     *   within the pojo failed assertion.
     * @param actualPojo
     * @param expectedPojo
     */
    protected void assertPojoEqual(BloatedPojo actualPojo, BloatedPojo expectedPojo) {
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

        // NOTE: can't check the pojo.streamValue() after calling the dao
        //   b/c 'inputStream' can no longer be read for a value (expected)
    }

    protected String extractString(Clob clob) {
        if (clob == null) {
            return null;
        }
        try (Reader reader = clob.getCharacterStream()) {
            return IOUtils.toString(reader);
        }
        catch (Exception e) {
            throw new RuntimeException("Error attempting to get string value from reader for Logging: " + e.getMessage(), e);
        }
    }
}
