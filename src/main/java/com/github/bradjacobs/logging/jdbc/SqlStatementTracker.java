package com.github.bradjacobs.logging.jdbc;

import com.github.bradjacobs.logging.jdbc.param.TagFiller;

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
    private final boolean streamLoggingEnabled;


    public SqlStatementTracker()
    {
        this.tagFiller = null;
        this.streamLoggingEnabled = false;
    }

    public SqlStatementTracker(String sql, TagFiller tagFiller, boolean streamLoggingEnabled)
    {
        this.sql = sql;
        this.tagFiller = tagFiller;
        this.streamLoggingEnabled = streamLoggingEnabled;
    }


    public boolean isStreamLoggingEnabled() {
        return streamLoggingEnabled;
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
