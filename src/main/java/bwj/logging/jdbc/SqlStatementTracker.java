package bwj.logging.jdbc;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SqlStatementTracker
{
    private String sql = "";

    private List<BatchItem> batchItems = null;
    private Map<Integer, Object> paramMap = null;



    private final TagFiller tagFiller;
    private final boolean logTextReaderStreams;


    public SqlStatementTracker()
    {
        this.tagFiller = null;
        this.logTextReaderStreams = false;
    }

    public SqlStatementTracker(String sql, TagFiller tagFiller, boolean logTextStreams)
    {
        this.sql = sql;
        this.tagFiller = tagFiller;
        this.logTextReaderStreams = logTextStreams;
    }

    public boolean canLogReaderStreams() {
        return logTextReaderStreams;
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
        this.batchItems.add(new BatchItem(sql, this.paramMap, this.tagFiller ));
    }

    public void clearBatch()
    {
        if (batchItems != null) {
            this.batchItems.clear();
        }
    }


    public String generateSql() {
        if (tagFiller == null) {
            return sql;
        }
        return tagFiller.replace(this.sql, this.paramMap);
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
            this.paramMap = new HashMap<>();
        }
        this.paramMap.put(index, parameter);
    }

    public void clearParameters() {
        if (this.paramMap != null)
            this.paramMap.clear();
    }


    /**
     * IF AND ONLY IF configured, this will allow logging of 'large' text param values.
     * Since the reader is 'read out' to obtain the string for logging,
     *  a new Reader object is returned to be used.
     *    WARNING:  this can potentially huge impact on memory usage.
     * @param index
     * @param parameter
     * @return
     */
    public Reader setReaderParameter(int index, Reader parameter) {
        if (! logTextReaderStreams) {
            this.setParameter(index, "_{text clob}_");
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




    /**
     * Represents each entry when 'batch' is used.
     */
    protected static class BatchItem {
        private final String sql;
        private final Map<Integer, Object> paramMap;
        private final TagFiller tagFiller;

        public BatchItem(String sql, Map<Integer, Object> paramMap, TagFiller tagFiller)
        {
            this.sql = sql;
            // batchItem makes it's own copy of the params so they don't get side-effected/modified.
            this.paramMap = new HashMap<>(paramMap);
            this.tagFiller = tagFiller;
        }

        public String generateSqlString()
        {
            if (tagFiller == null) {
                return sql;
            }
            return tagFiller.replace(this.sql, this.paramMap);
        }
    }

}
