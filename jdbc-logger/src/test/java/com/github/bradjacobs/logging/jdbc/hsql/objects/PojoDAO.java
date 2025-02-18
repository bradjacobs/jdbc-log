package com.github.bradjacobs.logging.jdbc.hsql.objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PojoDAO {
    private static final Logger log = LoggerFactory.getLogger(PojoDAO.class);

    private static final String TABLE_NAME = "pojos";
    private static final String[][] TABLE_COLUMN_DEFNS = {
            {"id", "INT", "IDENTITY"},
            {"name", "VARCHAR(30)"},
            {"intValue", "INT"},
            {"doubleValue", "DOUBLE"},
            {"sqlDateValue", "date"},
            {"sqlTimestampValue", "datetime"},
            {"clobValue", "CLOB(1000)"},
            {"streamValue", "VARCHAR(255)"},
    };

    protected Connection conn;

    private static final String DROP_TABLE_SQL = generateDropTableSql(TABLE_NAME);
    private static final String CREATE_TABLE_SQL = generateCreateTableSql(TABLE_NAME, TABLE_COLUMN_DEFNS);
    private static final String PREPARED_STMT_INSERT_SQL = generatePreparedStatementInsertIntoTableSql(TABLE_NAME, TABLE_COLUMN_DEFNS);
    private static final String STMT_SELECT_ALL_SQL = generateSelectAllSql(TABLE_NAME);
    private static final String PREPARED_STMT_SELECT_BY_ID_SQL = generatePreparedSttmentSelectByIdSql(TABLE_NAME);

    private static final String DROP_STORED_PROC_SQL = "DROP PROCEDURE EXT_SAMPLE_PROC IF EXISTS";

    // stored proc does nothing interesting, but has an IN and OUT parameter.
    private static final String CREATE_STORED_PROC_SQL =
        "CREATE PROCEDURE EXT_SAMPLE_PROC(IN id_in INT, OUT status_out BOOLEAN)\n" +
        " MODIFIES SQL DATA\n" +
        " BEGIN ATOMIC\n" +
        "\n" +
        " DECLARE temp_count INTEGER;\n" +
        " SET temp_count = -1;\n" +
        " SET status_out = FALSE;\n" +
        "\n" +
        "  select count(*) into temp_count from " + TABLE_NAME + " p where p.id = id_in;\n" +
        "  if temp_count > 0  THEN\n" +
        "      SET status_out = TRUE;\n" +
        " end if;\n" +
        " END";

    public String getSelectAllObjectsSQL() {
        return STMT_SELECT_ALL_SQL;
    }
    public String getSelectByIdSQL(int id) {
        return PREPARED_STMT_SELECT_BY_ID_SQL.replace("?", String.valueOf(id));
    }

    public PojoDAO(Connection conn) {
        this.conn = conn;
    }

    public void init() {
        createTable();
        createStoredProc();
    }

    public void close() {
        dropStoredProc();
        dropTable();
        try {
            this.conn.close();
        }
        catch (SQLException e) {
            /* ignore */
        }
    }

    public boolean createTable() {
        return executeSql(CREATE_TABLE_SQL);
    }
    public boolean dropTable() {
        return executeSql(DROP_TABLE_SQL);
    }

    public boolean createStoredProc() {
        return executeSql(CREATE_STORED_PROC_SQL);
    }
    public boolean dropStoredProc() {
        return executeSql(DROP_STORED_PROC_SQL);
    }

    public boolean executeSql(String sql) {
        boolean success = false;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.execute(sql);
            success = true;
        } catch (SQLException e) {
            throw new RuntimeException("Unable execute sql: " + e.getMessage(), e);
        } finally {
            closeQuietly(stmt);
        }
        return success;
    }

    public void insertPojo(BloatedPojo pojo, boolean useBatching) throws SQLException {
        insertPojos(Collections.singletonList(pojo), useBatching);
    }

    public void insertPojos(List<BloatedPojo> pojos, boolean useBatching) throws SQLException {
        PreparedStatement pstmt = null;
        try {
            if (useBatching) {
                conn.setAutoCommit(false);
            }
            pstmt = conn.prepareStatement(PREPARED_STMT_INSERT_SQL);
            for (BloatedPojo pojo : pojos) {
                initializeBloatedPojoPreparedStatement(pstmt, pojo);
                if (useBatching) {
                    pstmt.addBatch();
                }
                else {
                    pstmt.executeUpdate();
                }
            }
            if (useBatching) {
                int[] batchResponse = pstmt.executeBatch();
                conn.commit();
            }
        }
        catch (SQLException e) {
            throw new RuntimeException("Unable to insert batch into the table", e);
        }
        finally {
            conn.setAutoCommit(true);
            closeQuietly(pstmt);
        }
    }

    private void initializeBloatedPojoPreparedStatement(PreparedStatement pstmt, BloatedPojo pojo)
            throws SQLException
    {
        int paramIndex = 1;
        pstmt.setString(paramIndex++, pojo.getName());
        pstmt.setInt(paramIndex++, pojo.getIntValue());
        pstmt.setDouble(paramIndex++, pojo.getDoubleValue());
        pstmt.setDate(paramIndex++, pojo.getSqlDateValue());
        pstmt.setTimestamp(paramIndex++, pojo.getSqlTimestampValue());

        if (pojo.getClobValue() != null) {
            pstmt.setClob(paramIndex++, pojo.getClobValue());
        }
        else {
            pstmt.setNull(paramIndex++, Types.CLOB);
        }

        if (pojo.getStreamValue() != null) {
            pstmt.setAsciiStream(paramIndex++, pojo.getStreamValue());
        }
        else {
            pstmt.setNull(paramIndex++, Types.CLOB);
        }
    }

    public boolean batchexecuteSqlStatements(List<String> sqlStatements) throws SQLException
    {
        conn.setAutoCommit(false);
        Statement statement = null;
        try {
            statement = conn.createStatement();
            for (String sqlStatement : sqlStatements) {
                statement.addBatch(sqlStatement);
            }
            statement.executeBatch();
            conn.commit();
        }
        catch (SQLException e) {
            throw new RuntimeException("Unable to insert batch into the table", e);
        }
        finally {
            conn.setAutoCommit(true);
            closeQuietly(statement);
        }
        return true;
    }

    /**
     * Calls stored procedure
     * @param input  stored proc input value
     * @return return the "OUT value" from the stored procedure.
     */
    public Boolean callStoredProcedure(Integer input) {
        if (conn != null) {
            CallableStatement callableStatement = null;

            try {
                callableStatement = conn.prepareCall("CALL EXT_SAMPLE_PROC(?,?)");
                callableStatement.setInt(1, input);
                callableStatement.registerOutParameter(2, Types.BOOLEAN);
                //callableStatement.registerOutParameter(2, JDBCType.BOOLEAN);

                int executeUpdateResponse = callableStatement.executeUpdate();

                //read the OUT parameter now
                return callableStatement.getBoolean(2);
            }
            catch (SQLException e) {
                throw new RuntimeException("Unable to call stored proc " + e.getMessage(), e);
            }
            finally {
                closeQuietly(callableStatement);
            }
        }
        return null;
    }

    public void callStoredProcedureBatch(List<Integer> inputList) throws SQLException {
        if (conn != null) {
            CallableStatement callableStatement = null;
            conn.setAutoCommit(false);

            try {
                callableStatement = conn.prepareCall("CALL EXT_SAMPLE_PROC(?,?)");
                for (Integer intValue : inputList) {
                    callableStatement.setInt(1, intValue);
                    callableStatement.registerOutParameter(2, Types.BOOLEAN);
                    callableStatement.addBatch();
                }

                int[] batchResponseValues = callableStatement.executeBatch();
                for (int batchResponseValue : batchResponseValues) {
                    if (batchResponseValue != Statement.SUCCESS_NO_INFO) {
                        throw new RuntimeException("unexpected result code: " + batchResponseValue);
                    }
                }
            }
            catch (SQLException e) {
                throw new RuntimeException("Unable to call stored proc " + e.getMessage(), e);
            }
            finally {
                closeQuietly(callableStatement);
            }
        }
    }

    /**
     * Get all the pojos stored in the database
     * @return
     */
    public List<BloatedPojo> getAllPojos() throws SQLException {
        log.debug("getAllPojos()");
        return executeQuery(STMT_SELECT_ALL_SQL);
    }

    /**
     * Get a pojo by unique ID
     * @param id id
     * @return pojo (if found)
     */
    public BloatedPojo getPojoById(int id) throws SQLException {
        log.debug("getPojoById({})", id);

        PreparedStatement pstmt = conn.prepareStatement(PREPARED_STMT_SELECT_BY_ID_SQL);
        pstmt.setInt(1, id);
        List<BloatedPojo> resultList = executeQuery(pstmt);

        // expect there to only be 0 or 1 in this case
        if (resultList.size() > 1) {
            throw new InternalError("Invalid query response");
        }
        else if (resultList.size() == 1) {
            return resultList.get(0);
        }
        else {
            log.warn("No pojo found for id = {}", id);
            return null;
        }
    }

    private List<BloatedPojo> executeQuery(String sqlSelect) throws SQLException {
        return executeQuery(conn.createStatement(), sqlSelect);
    }
    private List<BloatedPojo> executeQuery(PreparedStatement pStmt) throws SQLException {
        return executeQuery(pStmt, null);
    }

    private List<BloatedPojo> executeQuery(Statement stmt, String sql) throws SQLException {
        try {
            ResultSet rs = null;
            // kludgy, even for test code
            if (stmt instanceof PreparedStatement) {
                rs = ((PreparedStatement)stmt).executeQuery();
            }
            else {
                rs = stmt.executeQuery(sql);
            }
            List<BloatedPojo> resultList = convertToPojos(rs);
            rs.close();
            return resultList;
        } catch (SQLException e) {
            throw new RuntimeException("Unable execute query: " + e.getMessage(), e);
        } finally {
            closeQuietly(stmt);
        }
    }

    private List<BloatedPojo> convertToPojos(ResultSet rs) throws SQLException {
        List<BloatedPojo> resultList = new ArrayList<>();
        while (rs.next()) {
            BloatedPojo pojo = new BloatedPojo();
            pojo.setId(rs.getInt("id"));
            pojo.setName(rs.getString("name"));
            pojo.setIntValue(rs.getInt("intValue"));
            pojo.setDoubleValue(rs.getDouble("doubleValue"));
            pojo.setSqlDateValue(rs.getDate("sqlDateValue"));
            pojo.setSqlTimestampValue(rs.getTimestamp("sqlTimestampValue"));
            pojo.setClobValue(rs.getClob("clobValue"));
            pojo.setStreamValue(rs.getAsciiStream("streamValue"));
            resultList.add(pojo);
        }
        return resultList;
    }

    private void closeQuietly(Statement stmt) {
        if (stmt != null) {
            try { stmt.close(); }
            catch (SQLException e) { /* ingore */ }
        }
    }

    private static String generateDropTableSql(String tableName) {
        return String.format("DROP TABLE %s IF EXISTS", tableName);
    }

    private static String generateCreateTableSql(String tableName, String[][] tableColumnDefns) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ");
        sb.append(tableName);
        sb.append(" (");
        // todo - probably a cooler way to do this
        for (int i = 0; i < tableColumnDefns.length; i++) {
            String[] columnDefns = tableColumnDefns[i];
            if (i > 0) {
                sb.append(", ");
            }
            String joinStr = String.join(" ", columnDefns);
            sb.append(joinStr);
        }
        sb.append(")");
        return sb.toString();
    }

    private static String generatePreparedStatementInsertIntoTableSql(String tableName, String[][] tableColumnDefns) {
        List<String> columnNames = getInsertColumnNames(tableColumnDefns);
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(tableName);
        sb.append(" (");
        sb.append(String.join(", ", columnNames));
        sb.append(") VALUES (");
        // TODO - adjust language level so can use the 'repeat'
        //        sb.append("?, ".repeat(columnNames.size()-1));
        for (int i = 0; i < columnNames.size() -1; i++) {
            sb.append("?, ");
        }
        sb.append("?)");
        return sb.toString();
    }

    private static String generateSelectAllSql(String tableName) {
        return String.format("SELECT * FROM %s", tableName);
    }
    private static String generatePreparedSttmentSelectByIdSql(String tableName) {
        return String.format("SELECT * FROM %s WHERE id = ?", tableName);
    }

    private static List<String> getInsertColumnNames(String[][] tableColumnDefns) {
        List<String> resultList = new ArrayList<>();
        for (String[] tableColumnDefn : tableColumnDefns) {
            if (tableColumnDefn[tableColumnDefn.length-1].equalsIgnoreCase("IDENTITY")) {
                continue;
            }
            resultList.add(tableColumnDefn[0]);
        }
        return resultList;
    }
}

/*
    For reference:
    to capture any "auto generated" ids, would do the following:

    pstmt = conn.prepareStatement(_SQL_, Statement.RETURN_GENERATED_KEYS);
    ...  execute pstmt ...
    ResultSet tableKeys = pstmt.getGeneratedKeys();
    if (tableKeys != null) {
        while (tableKeys.next()) {
            int autoGeneratedID = tableKeys.getInt(1);
            System.out.println("AutoGenKey: " + autoGeneratedID);
        }
    }
 */
