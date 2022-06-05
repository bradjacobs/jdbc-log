package com.github.bradjacobs.logging.jdbc.listeners;

public interface LoggingListener {
    void log(String sql);
}
