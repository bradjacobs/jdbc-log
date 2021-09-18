package com.github.bradjacobs.logging.jdbc.demo.dao;

import org.hibernate.dialect.Dialect;

import java.sql.Types;

/**
 * VERY simple dialect for SQLITE.
 *
 */
public class SQLDialect extends Dialect
{
    public SQLDialect() {
        registerColumnType(Types.BIT, "integer");
        registerColumnType(Types.TINYINT, "tinyint");
        registerColumnType(Types.SMALLINT, "smallint");
        registerColumnType(Types.INTEGER, "integer");
        registerColumnType( Types.BIT, "boolean" );
        registerColumnType(Types.FLOAT, "float");
        registerColumnType(Types.DOUBLE, "double");
        registerColumnType( Types.DECIMAL, "decimal" );
        registerColumnType( Types.CHAR, "char" );
        registerColumnType( Types.LONGVARCHAR, "longvarchar" );
        registerColumnType( Types.TIMESTAMP, "datetime" );
        registerColumnType( Types.BINARY, "blob" );
        registerColumnType( Types.VARBINARY, "blob" );
        registerColumnType( Types.LONGVARBINARY, "blob" );

        // other data types
    }

    @Override
    public String getAddColumnString() {
        return "add column";
    }
}