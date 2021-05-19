package bwj.logging.jdbc;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

class SqlStatementTracker
{
    private static final String TAG = "?";

    private Renderer renderer = new DefaultRenderer();

    private String sql = "";

    private List<BatchItem> batchItems = null;
    private SortedMap<Integer, Object> paramMap = null;


    private boolean logTextReaderStreams = true;

    private static final String TEXT_CLOB_VALUE_PLACEHOLDER = "{TextClob}";



    public SqlStatementTracker() {
        this(new DefaultRenderer());
    }

    public SqlStatementTracker(Renderer renderer) {
        this.renderer = renderer;
    }

    public void setSql(String sql)
    {
        this.sql = sql;
    }

    public void addBatch(String sql)
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



    public String generateSql() {
        return generateSql(this.sql, this.paramMap, this.renderer);
    }
    public String generateBatchSql() {
        if (this.batchItems == null || this.batchItems.size() == 0) {
            return "";
        }
        List<String> sqlList = new ArrayList<>();
        for (BatchItem batchItem : batchItems) {
            sqlList.add(batchItem.generateSqlString());
        }
        return sqlList.toString();
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
            this.setParameter(index, TEXT_CLOB_VALUE_PLACEHOLDER);
            return parameter;
        }
        else if (parameter == null) {
            this.setParameter(index, null);
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
            // todo - handle better
            e.printStackTrace();
            throw new RuntimeException("Error: " + e.getMessage(), e);
        }
    }

    public void clearParameters() {
        if (this.paramMap != null)
            this.paramMap.clear();
    }


    private static String generateSql(String sql, SortedMap<Integer, Object> paramMap, Renderer renderer) {
        if (paramMap == null || paramMap.size() == 0)
            return sql;
        return TagUpdater.replace(sql, TAG, paramMap.values().iterator(), renderer);
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

        public String generateSqlString() {
            return generateSql(this.sql, this.paramMap, this.renderer);
        }

    }

}
