package bwj.logging.jdbc;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class SqlStatementTracker
{
    private Renderer renderer = new DefaultRenderer();

    private String sql = "";

    private List<BatchItem> batchItems = null;
    private SortedMap<Integer, Object> paramMap = null;


    private boolean logTextReaderStreams = true;

    private static final String CLOB_VALUE_PLACEHOLDER = "{Clob}";
    private static final String READER_PLACEHOLDER = "{Reader}";



    public SqlStatementTracker() {
        this(new DefaultRenderer());
    }

    public SqlStatementTracker(Renderer renderer) {
        this.renderer = renderer;
    }


    public void setSql(String sql) {
        this.sql = sql;
    }


    public void addBatch(String sql) throws SQLException
    {
        this.sql = sql;
        addBatch();
    }

    public void addBatch()
    {
        if (this.batchItems == null) {
            this.batchItems = new ArrayList<>();
        }
        this.batchItems.add(new BatchItem(sql, this.paramMap, this.renderer ));
    }

    public void clearBatch()
    {
        if (batchItems != null) {
            this.batchItems.clear();
        }
    }

    private void logAndClearBatch() {

        System.out.println("******* LKGGING BATCH *******");

      //  log(this.batchItems.toString());
        clearBatch();
    }




    public void setParameter(int index, Object parameter) {
        if (this.paramMap == null) {
            this.paramMap = new TreeMap<>();
        }
        this.paramMap.put(index, parameter);
    }

    /**
     * If configured, this will allow logging of 'large' text param values.
     * Since the reader is 'read out' to obtain the string for logging,
     *  a new Reader object is returned to be used.
     *    WARNING:  this can potentially huge impact on memory usage.
     * @param index
     * @param parameter
     * @return
     */
    public Reader setReaderParameter(int index, Reader parameter) {
        if (! logTextReaderStreams) {
            return parameter;
        }
        else if (parameter == null) {
            return null;
        }

        try
        {
            String readerString = IOUtils.toString(parameter);
            this.setParameter(index, readerString);
            return new StringReader(readerString);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Error: " + e.getMessage(), e);
        }
    }

    public void clearParameters() {
        if (this.paramMap != null)
            this.paramMap.clear();
    }

    public String toString() {
        return generateSqlString(this.sql, this.paramMap, this.renderer);
    }


    /**
     * Represents each entry when 'batch' is used.
     */
    protected static class BatchItem {
        private final String sql;
        private final SortedMap<Integer, Object> paramMap;
        private final Renderer renderer;

        public BatchItem(String sql, SortedMap<Integer, Object> paramMap, Renderer renderer)
        {
            this.sql = sql;
            // batchItem makes it's own copy of the params so they don't get side-effected/modified.
            this.paramMap = new TreeMap<>(paramMap);
            this.renderer = renderer;
        }

        public String toString() {
            return generateSqlString(this.sql, this.paramMap, this.renderer);
        }
    }


    private static String generateSqlString(String sql, SortedMap<Integer, Object> paramMap, Renderer renderer) {
        if (paramMap == null || paramMap.size() == 0)
            return sql;
        return TagUpdater.replace(sql, "?", paramMap.values().iterator(), renderer);
    }


}
