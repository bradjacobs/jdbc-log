package com.github.bradjacobs.logging.jdbc.hsql;

import com.github.bradjacobs.logging.jdbc.hsql.objects.BloatedPojo;
import com.github.bradjacobs.logging.jdbc.hsql.objects.CaptureLoggingListener;
import com.github.bradjacobs.logging.jdbc.hsql.objects.PojoDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PojoLoggingTest extends AbstractPojoLoggingTest {
    private PojoDAO dao = null;
    private CaptureLoggingListener captureLoggingListener = null;

    private static final String CLOB_PARAM_PLACEHOLDER = "{_CLOB_}";

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
    public void addRetrievePojosNoBatch() throws Exception {
        addRetrievePojos(false);
    }

    @Test
    public void addRetrievePojosWithBatch() throws Exception {
        addRetrievePojos(true);
    }

    private void addRetrievePojos(boolean useBatch) throws Exception {
        long timeValue = 1538014031000L;    // '2018-09-27'

        String pojo1ClobString = "MY__TEST__CLOB";
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

        assertTrue(sql1.contains(CLOB_PARAM_PLACEHOLDER), "Logged SQL missing expected substring: " + CLOB_PARAM_PLACEHOLDER);
        assertTrue(sql2.contains(CLOB_PARAM_PLACEHOLDER), "Logged SQL missing expected substring: " + CLOB_PARAM_PLACEHOLDER);
        assertFalse(sql1.contains(pojo1ClobString), "Logged SQL should NOT contain substring: " + pojo1ClobString);
        assertFalse(sql2.contains(pojo2ClobString), "Logged SQL should NOT contain substring: " + pojo1ClobString);

        List<BloatedPojo> pojos = dao.getAllPojos();

        // confirm the returned results are as expected
        //    (i.e. confirm the logger didn't mess something up)
        assertEquals( pojos.size(), 2, "Wrong number of pojos");
        BloatedPojo retrievedPojo1 = pojos.get(0);
        BloatedPojo retrievedPojo2 = pojos.get(1);
        assertPojoEqual(retrievedPojo1, inputPojo1);
        assertPojoEqual(retrievedPojo2, inputPojo2);
        assertPojoEqual(dao.getPojoById(retrievedPojo1.getId()), retrievedPojo1);
        assertPojoEqual(dao.getPojoById(retrievedPojo2.getId()), retrievedPojo2);
    }

    @Test
    public void testQueryEmptyTable() throws SQLException {
        List<BloatedPojo> pojos = dao.getAllPojos();
        assertEquals(pojos.size(), 0, "wrong number of pojos");

        List<String> sqlStatements = captureLoggingListener.getSqlStatementStartingWith("SELECT");
        assertEquals(1, sqlStatements.size());

        String sqlStatement = sqlStatements.get(0);
        assertEquals("select * from pojos", sqlStatement.toLowerCase());
    }

    @Test
    public void testStatementQueryWithBatch() throws SQLException {
        BloatedPojo inputPojo1 = createDummyPojo("Bob");
        dao.insertPojo(inputPojo1, true);

        // execute 3 sql statements via batch.
        // only the 2nd one would do anything
        String sql1 = dao.getSelectByIdSQL(882);
        String sql2 = dao.getSelectAllObjectsSQL();
        String sql3 = dao.getSelectByIdSQL(1022);
        List<String> sqlStatements = Arrays.asList(sql1, sql2, sql3);

        dao.batchexecuteSqlStatements(sqlStatements);
        List<String> sqlStatementResults = this.captureLoggingListener.getSqlStatements();
        List<String> lastThreeSqlStatements = sqlStatementResults.subList(sqlStatementResults.size() - 3, sqlStatementResults.size());

        assertEquals(sqlStatements, lastThreeSqlStatements);
    }

    @Test
    public void testMultiplePreparedStatementBatches() throws SQLException {
        List<BloatedPojo> batchA = new ArrayList<>();
        List<BloatedPojo> batchB = new ArrayList<>();
        List<BloatedPojo> batchC = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            batchA.add(createDummyPojo("Batch_A_" + i));
            batchB.add(createDummyPojo("Batch_B_" + i));
            batchC.add(createDummyPojo("Batch_C_" + i));
        }

        dao.insertPojos(batchA, true);
        dao.insertPojos(batchB, true);
        dao.insertPojos(batchC, true);
        List<BloatedPojo> queriedPojos = dao.getAllPojos();

        List<String> sqlStatementResults = this.captureLoggingListener.getSqlStatementStartingWith("INSERT");
        assertEquals(9, sqlStatementResults.size());
        assertEquals(9, queriedPojos.size());
    }
}
