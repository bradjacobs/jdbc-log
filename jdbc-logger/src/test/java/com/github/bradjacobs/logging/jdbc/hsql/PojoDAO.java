
package com.github.bradjacobs.logging.jdbc.hsql;

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
import java.util.List;

public class PojoDAO
{
    private static final String TABLE_NAME = "pojos";

    private static final Logger log = LoggerFactory.getLogger(PojoDAO.class);

    protected Connection conn;


    private static final String DROP_TABLE_SQL = "DROP TABLE " + TABLE_NAME + " IF EXISTS";

    private static final String CREATE_TABLE_SQL =
        "CREATE TABLE " + TABLE_NAME +
        " (id INT IDENTITY," +  // let the table auto-assign an id
        " name VARCHAR(30)," +
        " intValue INT," +
        " doubleValue DOUBLE," +
        " sqlDateValue date," +
        " sqlTimestampValue datetime," +
        " clobValue CLOB(1000)," +
        " streamValue VARCHAR(255))";

    private static final String PREPARED_STMT_INSERT_SQL =
        "INSERT INTO " + TABLE_NAME + " VALUES (null, ?, ?, ?, ?, ?, ?, ?)";
    private static final String STMT_SELECT_ALL_SQL = "SELECT * FROM " + TABLE_NAME;
    private static final String PREPARED_STMT_SELECT_BY_ID_SQL = STMT_SELECT_ALL_SQL + " WHERE id = ?";


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


    public PojoDAO(Connection conn)
    {
        this.conn = conn;
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


    private boolean executeSql(String sql)
    {
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


    public boolean batchinsertPojo(List<BloatedPojo> pojos) throws SQLException
    {
        conn.setAutoCommit(false);
        PreparedStatement pstmt = null;

        try {
            pstmt = conn.prepareStatement(PREPARED_STMT_INSERT_SQL);

            for (BloatedPojo pojo : pojos) {
                initializeBloatedPojoPreparedStatement(pstmt, pojo);
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            conn.commit();
        }
        catch (SQLException e) {
            throw new RuntimeException("Unable to insert batch into the table", e);
        }
        finally {
            conn.setAutoCommit(true);
            closeQuietly(pstmt);
        }

        return true;
    }


    /**
     * Insert a pojo into the database
     * @return
     */
    public boolean insertPojo(BloatedPojo pojo) {
        boolean success = false;

        if (pojo != null) {
            log.debug("insertPojo({})", pojo.getName());
            if (conn != null) {
                PreparedStatement pstmt = null;

                try {
                    pstmt = conn.prepareStatement(PREPARED_STMT_INSERT_SQL);

                    initializeBloatedPojoPreparedStatement(pstmt, pojo);

                    success = (pstmt.executeUpdate() == 1);  // success means exactly one row inserted
                } catch (SQLException e) {
                    throw new RuntimeException("Unable to insert" + pojo.getName() + " into the table", e);
                } finally {
                    closeQuietly(pstmt);
                }
            }
        }
        return success;
    }

    private void initializeBloatedPojoPreparedStatement(PreparedStatement pstmt, BloatedPojo pojo)
            throws SQLException
    {
        pstmt.setString(1, pojo.getName());
        pstmt.setInt(2, pojo.getIntValue());
        pstmt.setDouble(3, pojo.getDoubleValue());
        pstmt.setDate(4, pojo.getSqlDateValue());
        pstmt.setTimestamp(5, pojo.getSqlTimestampValue());

        if (pojo.getClobValue() != null) {
            pstmt.setClob(6, pojo.getClobValue());
        }
        else {
            pstmt.setNull(7, Types.CLOB);
        }

        if (pojo.getStreamValue() != null) {
            pstmt.setAsciiStream(7, pojo.getStreamValue());
        }
        else {
            pstmt.setNull(7, Types.CLOB);
        }
    }


    public void callStoredProcedure(Integer input)
    {
        if (conn != null) {
            CallableStatement pstmt = null;

            try {
               // pstmt = conn.prepareCall("{call EXT_SAMPLE_PROC(?,?)}");
                pstmt = conn.prepareCall("CALL EXT_SAMPLE_PROC(?,?)");

                pstmt.setInt(1, input);
                pstmt.registerOutParameter(2, Types.BOOLEAN);

                pstmt.executeUpdate();

                //read the OUT parameter now
                Boolean result = pstmt.getBoolean(2);
                //System.out.println("RESULT: " + result);
            }
            catch (SQLException e) {
                throw new RuntimeException("Unable to call stored proc " + e.getMessage(), e);
            }
            finally {
                closeQuietly(pstmt);
            }
        }
    }


    /**
     * Get all the pojos stored in the database
     *
     * @return
     */
    public List<BloatedPojo> getAllPojos() {
        log.debug("getAllPojos()");
        List<BloatedPojo> pojos = new ArrayList<BloatedPojo>();
        if (conn != null) {
            Statement stmt = null;

            try {
                stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(STMT_SELECT_ALL_SQL);
                pojos = convertToPojos(rs);
            } catch (SQLException e) {
                throw new RuntimeException("Unexpected SQLException: " + e.getMessage(), e);
            } finally {
                closeQuietly(stmt);
            }
        }

        return pojos;
    }

    /**
     * Get a pojo by unique ID
     * @param id
     * @return
     */
    public BloatedPojo getPojoById(int id) {
        log.debug("getPojoById({})", id);
        BloatedPojo pojo = null;
        if (conn != null) {
            PreparedStatement pstmt = null;

            try {
                pstmt = conn.prepareStatement(PREPARED_STMT_SELECT_BY_ID_SQL);
                pstmt.setInt(1, id);

                ResultSet rs = pstmt.executeQuery();

                List<BloatedPojo> resultList = convertToPojos(rs);

                // expect there to only be 0 or 1 in this case
                if (resultList.size() > 1) {
                    throw new InternalError("Invalid query response");
                }
                else if (resultList.size() == 1) {
                    pojo = resultList.get(0);
                }
                else {
                    log.warn("No pojo found for id = {}", id);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Unable query pojo by ID(" + id + ")", e);
            } finally {
                closeQuietly(pstmt);
            }
        }

        return pojo;
    }

    private List<BloatedPojo> convertToPojos(ResultSet rs) throws SQLException
    {
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
            try {
                stmt.close();
            }
            catch (SQLException e) {
                /* ingore */
            }
        }
    }
}
