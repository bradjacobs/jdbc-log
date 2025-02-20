package com.github.bradjacobs.logging.jdbc;

/**
 * TODO: this class will (probably) get removed.
 */
public enum DatabaseType {
    ORACLE,
    DEFAULT;


    /**
     * Attempt to identify db type via string
     * @param dbName can be database product name, jdbc connection url, driver name, other, etc.
     * @return DatabaseType based on input parameter
     */
    public static DatabaseType identifyDatabaseType(String dbName) {
        if (dbName == null) {
            return DEFAULT;
        }

        dbName = dbName.toUpperCase();
        if (dbName.contains("ORACLE")) { return ORACLE; }
        else { return DEFAULT; }
    }
}
