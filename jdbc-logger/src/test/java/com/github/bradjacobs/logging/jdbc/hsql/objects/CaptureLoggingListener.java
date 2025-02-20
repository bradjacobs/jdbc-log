package com.github.bradjacobs.logging.jdbc.hsql.objects;

import com.github.bradjacobs.logging.jdbc.listeners.LoggingListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ONLY FOR UNITTESTS!
 */
public class CaptureLoggingListener implements LoggingListener {
    private final List<String> sqlStatementList = new ArrayList<>();

    @Override
    public void log(String sql) {
        sqlStatementList.add(sql);
    }

    public List<String> getSqlStatementList() {
        return sqlStatementList;
    }

    public List<String> getSqlStatements() {
        return this.sqlStatementList;
    }

    public List<String> getSqlStatementStartingWith(String prefix) {
        String lowerPrefix = prefix.toLowerCase();
        return sqlStatementList.stream()
                .filter(s -> s.toLowerCase().startsWith(lowerPrefix))
                .collect(Collectors.toList());
    }
}
