package com.github.bradjacobs.logging.jdbc.hsql;

import com.github.bradjacobs.logging.jdbc.LoggingConnectionCreator;
import com.github.bradjacobs.logging.jdbc.LoggingListener;
import com.github.bradjacobs.logging.jdbc.listeners.SystemOutLogListener;
import org.apache.commons.io.IOUtils;
import org.hsqldb.jdbc.JDBCClob;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;


public class PojoLoggingTest
{
    private Connection dbcon = null;
    private PojoDAO dao = null;
    private CaptureLoggingListener captureLoggingListener = null;


    /**
     * Gets called before every test method
     * @throws SQLException
     */
    @BeforeMethod
    public void setup() throws SQLException {

        captureLoggingListener = new CaptureLoggingListener();

        Connection innerConn = DriverManager.getConnection("jdbc:hsqldb:mem:sampleDB", "SA", "");

        LoggingConnectionCreator loggingConnectionCreator =
            LoggingConnectionCreator.builder()
                .withLogListener(captureLoggingListener)
                .withLogListener(new SystemOutLogListener())
                .setStreamLoggingEnabled(true)
                .build();

        dbcon = loggingConnectionCreator.getConnection(innerConn);
        dao = new PojoDAO(dbcon);
        dao.createTable();
        dao.createStoredProc();
    }

    /**
     * Gets called after every test method
     */
    @AfterMethod
    public void tearDown() {
        dao.dropStoredProc();
        dao.dropTable();
        try {
            dbcon.close();
        }
        catch (SQLException e) {
            /* ignore */
        }
    }


    @Test
    public void addRetrievePojosNoBatch() throws Exception
    {
        addRetrievePojos(false);
    }

    @Test
    public void addRetrievePojosWithBatch() throws Exception
    {
        addRetrievePojos(true);
    }

    private void addRetrievePojos(boolean useBatch) throws Exception
    {
        long timeValue = 1538014031000L;    // '2018-09-27'

        String pojo1StreamString = null;
        String pojo2StreamString = "test_stream_2";

        BloatedPojo inputPojo1 = createTestPojo(
            null,
            "Rob",
            30,
            0d,
            timeValue,
            timeValue,
            "MY__TEST__CLOB",
            pojo1StreamString);

        BloatedPojo inputPojo2 = createTestPojo(
            null,
            "Aphrodite",
            59,
            88.3d,
            timeValue,
            timeValue,
            "MY__TEST__CLOB_2",
            pojo2StreamString);


        if (useBatch)
        {
            dao.batchinsertPojo(Arrays.asList(inputPojo1, inputPojo2));
        }
        else {
            assertTrue(dao.insertPojo(inputPojo1), "Unable to insert pojo1");
            assertTrue(dao.insertPojo(inputPojo2), "Unable to insert pojo2");
        }

        List<String> insertSqlStatements = this.captureLoggingListener.getSqlStatementStartingWith("INSERT");
        assertEquals(insertSqlStatements.size(), 2, "expected exactly 2 'INSERT' sql statements");

        List<BloatedPojo> pojos = dao.getAllPojos();
        assertEquals( pojos.size(), 2, "Wrong number of pojos");



        // TODO - clean up mess below

        BloatedPojo retreivedPojo1 = pojos.get(0);
        BloatedPojo retrievedPojo2 = pojos.get(1);

//        // reset the stream on the 'expected'
//        if (pojo1StreamString != null) {
//            inputPojo1.setStreamValue(createInputStreamValue(pojo1StreamString));
//        }
//        if (pojo2StreamString != null) {
//            inputPojo2.setStreamValue(createInputStreamValue(pojo2StreamString));
//        }

        assertPojoEqual(retreivedPojo1, inputPojo1);
        assertPojoEqual(retrievedPojo2, inputPojo2);

        assertPojoEqual(dao.getPojoById(retreivedPojo1.getId()), retreivedPojo1);
        assertPojoEqual(dao.getPojoById(retrievedPojo2.getId()), retrievedPojo2);
    }

    @Test
    public void testCallStoredProc()  throws Exception
    {
        long timeValue = 1538014031000L;    // '2018-09-27'

        String pojo1StreamString = null;
        String pojo2StreamString = "test_stream_2";

        BloatedPojo inputPojo1 = createTestPojo(
            null,
            "Rob",
            30,
            0d,
            timeValue,
            timeValue,
            "MY__TEST__CLOB",
            pojo1StreamString);

        BloatedPojo inputPojo2 = createTestPojo(
            null,
            "Aphrodite",
            59,
            88.3d,
            timeValue,
            timeValue,
            "MY__TEST__CLOB_2",
            pojo2StreamString);

        dao.batchinsertPojo(Arrays.asList(inputPojo1, inputPojo2));

        dao.callStoredProcedure(1);

    }


    @Test
    public void testQueryEmptyTable() {
        List<BloatedPojo> pojos = dao.getAllPojos();
        assertEquals(pojos.size(), 0, "wrong number of pojos");

        List<String> sqlStatements = captureLoggingListener.getSqlStatementStartingWith("SELECT");
        assertEquals(sqlStatements.size(), 1);

        String sqlStatement = sqlStatements.get(0);
        assertEquals(sqlStatement.toLowerCase(), "select * from pojos");


    }







    private BloatedPojo createTestPojo(Integer id, String name, int intValue, double doubleValue,
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


    private void assertPojoEqual(BloatedPojo actualPojo, BloatedPojo expectedPojo)
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
        assertEquals( extractString(actualPojo.getClobValue()), extractString(expectedPojo.getClobValue()) );
        //assertEquals( extractString(actualPojo.getStreamValue()), extractString(expectedPojo.getStreamValue()) );
    }


    private InputStream createInputStreamValue(String value)
    {
        return new ByteArrayInputStream("test_stream_1".getBytes(StandardCharsets.UTF_8));
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


    private static class CaptureLoggingListener implements LoggingListener
    {
        private final List<String> sqlStatementList = new ArrayList<>();

        @Override
        public void log(String sql) {
            sqlStatementList.add(sql);
        }

        public List<String> getSqlStatementList() {
            return sqlStatementList;
        }

        public List<String> getSqlStatementStartingWith(String prefix) {
            List<String> resultList = new ArrayList<>();
            String lowerPrefix = prefix.toLowerCase();

            for (String sql : sqlStatementList)
            {
                String lowerSql = sql.toLowerCase();
                if (lowerSql.startsWith(lowerPrefix)) {
                    resultList.add(sql);
                }
            }
            return resultList;
        }
    }




    private String extractString(Clob clob)
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

    private String extractString(InputStream inputStream)
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
