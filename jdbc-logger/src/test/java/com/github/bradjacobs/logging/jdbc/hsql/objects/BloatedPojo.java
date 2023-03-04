package com.github.bradjacobs.logging.jdbc.hsql.objects;

import java.io.InputStream;
import java.sql.Clob;

/**
 * Simple object that has different member variable types for testing.
 */
public class BloatedPojo {
    private int id;
    private String name;
    private int intValue;
    private double doubleValue;

    private java.sql.Date sqlDateValue;
    private java.sql.Timestamp sqlTimestampValue;

    private Clob clobValue;
    private InputStream streamValue;

    public BloatedPojo() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public java.sql.Date getSqlDateValue() {
        return sqlDateValue;
    }

    public void setSqlDateValue(java.sql.Date sqlDateValue) {
        this.sqlDateValue = sqlDateValue;
    }

    public java.sql.Timestamp getSqlTimestampValue() {
        return sqlTimestampValue;
    }

    public void setSqlTimestampValue(java.sql.Timestamp sqlTimestampValue) {
        this.sqlTimestampValue = sqlTimestampValue;
    }

    public Clob getClobValue() {
        return clobValue;
    }

    public void setClobValue(Clob clobValue) {
        this.clobValue = clobValue;
    }

    public InputStream getStreamValue() {
        return streamValue;
    }

    public void setStreamValue(InputStream streamValue)
    {
        this.streamValue = streamValue;
    }
}
