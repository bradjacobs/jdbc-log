package com.github.bradjacobs.logging.jdbc;

import com.github.bradjacobs.logging.jdbc.param.SqlTagFiller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SqlStatementTracker {
    private String sql;
    private final SqlTagFiller sqlTagFiller;

    private List<BatchItem> batchItems = null;
    private Map<Integer, Object> paramMap = null;

    public SqlStatementTracker() {
        this("", null);
    }

    public SqlStatementTracker(String sql, SqlTagFiller sqlTagFiller) {
        this.sql = sql;
        this.sqlTagFiller = sqlTagFiller;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public void addBatch(String sql) {
        this.sql = sql;
        addBatch();
    }

    public void addBatch() {
        if (this.batchItems == null) {
            this.batchItems = new ArrayList<>();
        }
        this.batchItems.add(new BatchItem(sql, this.paramMap, this.sqlTagFiller));
    }

    public void clearBatch() {
        if (batchItems != null) {
            this.batchItems.clear();
        }
    }

    public String generateSql() {
        if (sqlTagFiller == null) {
            return sql;
        }
        return sqlTagFiller.replace(this.sql, this.paramMap);
    }

    public List<String> generateBatchSql() {
        if (this.batchItems == null || this.batchItems.size() == 0) {
            return Collections.emptyList();
        }
        List<String> sqlList = new ArrayList<>();
        for (BatchItem batchItem : batchItems) {
            sqlList.add(batchItem.generateSqlString());
        }
        return sqlList;
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
        private final SqlTagFiller sqlTagFiller;

        public BatchItem(String sql, Map<Integer, Object> paramMap, SqlTagFiller sqlTagFiller) {
            this.sql = sql;
            this.sqlTagFiller = sqlTagFiller;
            if (sqlTagFiller != null && paramMap != null) {
                // batchItem makes its own copy of the params, so they don't get side-effected/modified.
                this.paramMap = new HashMap<>(paramMap);
            }
            else {
                this.paramMap = null;
            }
        }

        public String generateSqlString() {
            if (sqlTagFiller == null) {
                return sql;
            }
            return sqlTagFiller.replace(this.sql, this.paramMap);
        }
    }
}
