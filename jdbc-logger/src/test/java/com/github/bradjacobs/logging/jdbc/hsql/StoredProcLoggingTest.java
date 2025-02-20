package com.github.bradjacobs.logging.jdbc.hsql;

import com.github.bradjacobs.logging.jdbc.hsql.objects.BloatedPojo;
import com.github.bradjacobs.logging.jdbc.hsql.objects.CaptureLoggingListener;
import com.github.bradjacobs.logging.jdbc.hsql.objects.PojoDAO;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;


public class StoredProcLoggingTest extends AbstractPojoLoggingTest {
    private PojoDAO dao = null;
    private CaptureLoggingListener captureLoggingListener = null;

    /**
     * Gets called before every test method
     */
    @BeforeMethod
    public void setup() throws Exception {
        captureLoggingListener = new CaptureLoggingListener();
        dao = initializePojoDao(captureLoggingListener, false);
    }

    /**
     * Gets called after every test method
     */
    @AfterMethod
    public void tearDown() {
        dao.close();
    }

    @Test
    public void testCallStoredProc()  throws Exception {
        long timeValue = 1538014031000L;    // '2018-09-27'

        String pojo1StreamString = null;
        String pojo2StreamString = "test_stream_2";

        BloatedPojo inputPojo1 = BloatedPojo.builder()
                .id(null).name("Rob").invValue(30).doubleValue(0d)
                .sqlDate(timeValue).sqlTimestamp(timeValue)
                .clobString("MY__TEST__CLOB").inputStreamString(pojo1StreamString)
                .build();

        BloatedPojo inputPojo2 = BloatedPojo.builder()
                .id(null).name("Aphrodite").invValue(59).doubleValue(88.3d)
                .sqlDate(timeValue).sqlTimestamp(timeValue)
                .clobString("MY__TEST__CLOB_2").inputStreamString(pojo2StreamString)
                .build();

        dao.insertPojos(Arrays.asList(inputPojo1, inputPojo2), true);
        Object outParam = dao.callStoredProcedure(1);

        List<String> callSqlStatements = this.captureLoggingListener.getSqlStatementStartingWith("CALL");
        assertEquals(callSqlStatements.size(), 1, "mismatch count of 'CALL' sql statements");
        assertEquals(callSqlStatements.get(0), "CALL EXT_SAMPLE_PROC(1,'{_OUT_BOOLEAN_}')");

        // confirm that the stored proc worked as expected.
        assertNotNull(outParam);
        assertEquals(Boolean.TRUE, outParam);
    }

    @Test
    public void testCallStoredProcAsBatch()  throws Exception {
        BloatedPojo inputPojo1 = createDummyPojo("Fredo");
        List<Integer> inputList = Arrays.asList(1,2,3);
        dao.callStoredProcedureBatch(inputList);

        List<String> callSqlStatements = this.captureLoggingListener.getSqlStatementStartingWith("CALL");
        assertEquals(callSqlStatements.size(), 3, "mismatch count of 'CALL' sql statements");
        assertEquals(callSqlStatements.get(0), "CALL EXT_SAMPLE_PROC(1,'{_OUT_BOOLEAN_}')");
        assertEquals(callSqlStatements.get(1), "CALL EXT_SAMPLE_PROC(2,'{_OUT_BOOLEAN_}')");
        assertEquals(callSqlStatements.get(2), "CALL EXT_SAMPLE_PROC(3,'{_OUT_BOOLEAN_}')");
    }
}
