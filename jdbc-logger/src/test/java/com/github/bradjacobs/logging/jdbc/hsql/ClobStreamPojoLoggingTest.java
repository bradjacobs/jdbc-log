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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClobStreamPojoLoggingTest extends AbstractPojoLoggingTest {
    private PojoDAO dao = null;
    private CaptureLoggingListener captureLoggingListener = null;

    // pre-test setup
    @BeforeEach
    public void setup() throws Exception {
        captureLoggingListener = new CaptureLoggingListener();
        dao = initializePojoDao(captureLoggingListener, true);
    }

    // post-test teardown
    @AfterEach
    public void tearDown() {
        dao.close();
    }

    @Test
    public void addRetrievePojosNoBatch() throws Exception {
        addRetrievePojos(false);
    }

    @Test
    public void addRetrievePojosWithBatch() throws Exception {
        addRetrievePojos(true);
    }

    private void addRetrievePojos(boolean useBatch) throws Exception {
        long timeValue = 1538014031000L;    // '2018-09-27'

        String pojo1ClobString = "MY__TEST__CLOB_1";
        String pojo2ClobString = "MY__TEST__CLOB_2";
        String pojo1StreamString = null;
        String pojo2StreamString = "test_stream_2";

        BloatedPojo inputPojo1 = BloatedPojo.builder()
                .id(null).name("Rob").invValue(30).doubleValue(0d)
                .sqlDate(timeValue).sqlTimestamp(timeValue)
                .clobString(pojo1ClobString).inputStreamString(pojo1StreamString)
                .build();

        BloatedPojo inputPojo2 = BloatedPojo.builder()
                .id(null).name("Aphrodite").invValue(59).doubleValue(88.3d)
                .sqlDate(timeValue).sqlTimestamp(timeValue)
                .clobString(pojo2ClobString).inputStreamString(pojo2StreamString)
                .build();

        dao.insertPojos(Arrays.asList(inputPojo1, inputPojo2), useBatch);

        List<String> insertSqlStatements = this.captureLoggingListener.getSqlStatementStartingWith("INSERT");
        assertEquals(2, insertSqlStatements.size(), "expected exactly 2 'INSERT' sql statements");

        String sql1 = insertSqlStatements.get(0);
        String sql2 = insertSqlStatements.get(1);

        assertTrue(sql1.contains(pojo1ClobString), "Logged SQL missing expected substring: " + pojo1ClobString);
        assertTrue(sql2.contains(pojo2ClobString), "Logged SQL missing expected substring: " + pojo1ClobString);
        assertTrue(sql2.contains(pojo2StreamString), "Logged SQL missing expected substring: " + pojo2StreamString);

        List<BloatedPojo> pojos = dao.getAllPojos();

        // confirm the returned results are as expected
        //    (i.e. confirm the logger didn't mess something up)
        assertEquals(2, pojos.size(),"Wrong number of pojos");
        BloatedPojo retrievedPojo1 = pojos.get(0);
        BloatedPojo retrievedPojo2 = pojos.get(1);
        assertPojoEqual(retrievedPojo1, inputPojo1);
        assertPojoEqual(retrievedPojo2, inputPojo2);
        assertPojoEqual(dao.getPojoById(retrievedPojo1.getId()), retrievedPojo1);
        assertPojoEqual(dao.getPojoById(retrievedPojo2.getId()), retrievedPojo2);
    }
}