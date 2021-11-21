package com.github.bradjacobs.logging.jdbc.hsql;

import com.github.bradjacobs.logging.jdbc.hsql.objects.BloatedPojo;
import com.github.bradjacobs.logging.jdbc.hsql.objects.CaptureLoggingListener;
import com.github.bradjacobs.logging.jdbc.hsql.objects.PojoDAO;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;


public class StoredProcLoggingTest extends AbstractPojoLoggingTest
{
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

        Object outParam = dao.callStoredProcedure(1);

        List<String> callSqlStatements = this.captureLoggingListener.getSqlStatementStartingWith("CALL");
        assertEquals(callSqlStatements.size(), 1, "mismatch count of 'CALL' sql statements");
        assertEquals(callSqlStatements.get(0), "CALL EXT_SAMPLE_PROC(1,'{_OUT_BOOLEAN_}')");

        // confirm that the stored proc worked as expected.
        assertNotNull(outParam);
        assertEquals(Boolean.TRUE, outParam);
    }

    @Test
    public void testCallStoredProcAsBatch()  throws Exception
    {
        long timeValue = 1538014031000L;    // '2018-09-27'
        BloatedPojo inputPojo1 = createTestPojo(
            null,
            "Rob",
            30,
            0d,
            timeValue,
            timeValue,
            "MY__TEST__CLOB",
            null);

        List<Integer> inputList = Arrays.asList(1,2,3);
        dao.callStoredProcedureBatch(inputList);

        List<String> callSqlStatements = this.captureLoggingListener.getSqlStatementStartingWith("CALL");
        assertEquals(callSqlStatements.size(), 3, "mismatch count of 'CALL' sql statements");
        assertEquals(callSqlStatements.get(0), "CALL EXT_SAMPLE_PROC(1,'{_OUT_BOOLEAN_}')");
    }
}
