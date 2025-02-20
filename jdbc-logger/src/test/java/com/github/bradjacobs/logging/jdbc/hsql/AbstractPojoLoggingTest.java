package com.github.bradjacobs.logging.jdbc.hsql;

import com.github.bradjacobs.logging.jdbc.DbLoggingBuilder;
import com.github.bradjacobs.logging.jdbc.hsql.objects.BloatedPojo;
import com.github.bradjacobs.logging.jdbc.hsql.objects.CaptureLoggingListener;
import com.github.bradjacobs.logging.jdbc.hsql.objects.PojoDAO;
import com.github.bradjacobs.logging.jdbc.listeners.SystemOutLogListener;
import org.apache.commons.io.IOUtils;
import org.hsqldb.jdbc.JDBCClob;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;


abstract public class AbstractPojoLoggingTest {
    protected PojoDAO initializePojoDao(CaptureLoggingListener captureLoggingListener, boolean clobLoggingEnabled) throws Exception {
        Connection innerConn = DriverManager.getConnection("jdbc:hsqldb:mem:sampleDB", "SA", "");

        DbLoggingBuilder dbLoggingBuilder =
                DbLoggingBuilder.builder(captureLoggingListener, new SystemOutLogListener())
                        .setClobParamLogging(clobLoggingEnabled);

        Connection dbcon = dbLoggingBuilder.createFrom(innerConn);
        PojoDAO pojoDao = new PojoDAO(dbcon);
        pojoDao.init();
        return pojoDao;
    }


    protected static class BloatedPojoBuilder {
        private Integer id = null;
        private String name = null;
        private int intValue = 0;
        private double doubleValue = 0.0d;
        private Long sqlDate = null;
        private Long sqlTimestamp = null;
        private String clobString = null;
        private String inputStreamString = null;

        private BloatedPojoBuilder() {}

        public static BloatedPojoBuilder builder() {
            return new BloatedPojoBuilder();
        }

        public BloatedPojoBuilder id(Integer id) { this.id = id; return this; }
        public BloatedPojoBuilder name(String name) { this.name = name; return this; }
        public BloatedPojoBuilder invValue(int intValue) { this.intValue = intValue; return this; }
        public BloatedPojoBuilder doubleValue(double doubleValue) { this.doubleValue = doubleValue; return this; }
        public BloatedPojoBuilder sqlDate(Long sqlDate) { this.sqlDate = sqlDate; return this; }
        public BloatedPojoBuilder sqlTimestamp(Long sqlTimestamp) { this.sqlTimestamp = sqlTimestamp; return this; }
        public BloatedPojoBuilder clobString(String clobString) { this.clobString = clobString; return this; }
        public BloatedPojoBuilder inputStreamString(String inputStreamString) { this.inputStreamString = inputStreamString; return this; }

        public BloatedPojo build() {
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
    }

    protected BloatedPojo createDummyPojo(String name) {
        return BloatedPojoBuilder.builder()
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

    private static InputStream createInputStreamValue(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }

    private static Clob createClobValue(String value) {
        try {
            return new JDBCClob(value);
        }
        catch (SQLException e) {
            throw new RuntimeException("Unable create clob: " + e.getMessage(), e);
        }
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
