package com.github.bradjacobs.logging.jdbc;


/**
 * @deprecated
 */
public enum DatabaseType
{
    DB2,
    DERBY,
    H2,
    HSQL,
    INFORMIX,
    MYSQL,
    ORACLE,
    POSTGRES,
    SQLITE,
    SQLSERVER,
    SYBASE,
    // many many more ...

    UNKNOWN;


    /**
     * Attempt to identify db type via string
     * @param dbName can be database product name, jdbc connection url, driver name, other, etc.
     * @return DatabaseType based on input parameter
     */
    public static DatabaseType identifyDatabaseType(String dbName)
    {
        if (dbName == null) {
            return UNKNOWN;
        }

        dbName = dbName.toUpperCase();
        if (dbName.contains("DB2")) { return DB2; }
        else if (dbName.contains("DERBY")) { return DERBY; }
        else if (dbName.contains("H2")) { return H2; }
        else if (dbName.contains("HSQL")) { return HSQL; }
        else if (dbName.contains("INFORMIX")) { return INFORMIX; }
        else if (dbName.contains("MYSQL")) { return MYSQL; }
        else if (dbName.contains("ORACLE")) { return ORACLE; }
        else if (dbName.contains("POSTGRES")) { return POSTGRES; }
        else if (dbName.contains("SQLITE")) { return SQLITE; }
        else if (dbName.contains("SQLSERVER") || dbName.contains("SQL SERVER")) { return SQLSERVER; }
        else if (dbName.contains("SYBASE")) { return SYBASE; }
        else { return UNKNOWN; }
    }
}
