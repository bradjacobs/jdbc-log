package com.github.bradjacobs.logging.jdbc.hsql;

import com.github.bradjacobs.logging.jdbc.hsql.objects.BloatedPojo;
import com.github.bradjacobs.logging.jdbc.hsql.objects.CaptureLoggingListener;
import com.github.bradjacobs.logging.jdbc.hsql.objects.PojoDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class StoredProcLoggingTest extends AbstractPojoLoggingTest {
    private PojoDAO dao = null;
    private CaptureLoggingListener captureLoggingListener = null;

    // pre-test setup
    @BeforeEach
    public void setup() throws Exception {
        captureLoggingListener = new CaptureLoggingListener();
        dao = initializePojoDao(captureLoggingListener, false);
    }

    // post-test teardown
    @AfterEach
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
        Boolean outParam = dao.callStoredProcedure(1);

        List<String> callSqlStatements = this.captureLoggingListener.getSqlStatementStartingWith("CALL");
        assertEquals(1, callSqlStatements.size(), "mismatch count of 'CALL' sql statements");
        assertEquals("CALL EXT_SAMPLE_PROC(1,'{_OUT_BOOLEAN_}')", callSqlStatements.get(0) );

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
        assertEquals(3, callSqlStatements.size(),"mismatch count of 'CALL' sql statements");
        assertEquals("CALL EXT_SAMPLE_PROC(1,'{_OUT_BOOLEAN_}')", callSqlStatements.get(0));
        assertEquals("CALL EXT_SAMPLE_PROC(2,'{_OUT_BOOLEAN_}')", callSqlStatements.get(1));
        assertEquals("CALL EXT_SAMPLE_PROC(3,'{_OUT_BOOLEAN_}')", callSqlStatements.get(2));
    }
}
