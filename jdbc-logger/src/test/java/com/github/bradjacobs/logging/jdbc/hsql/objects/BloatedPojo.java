package com.github.bradjacobs.logging.jdbc.hsql.objects;

import org.hsqldb.jdbc.JDBCClob;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.sql.SQLException;

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

    public static BloatedPojoBuilder builder() {
        return new BloatedPojoBuilder();
    }

    public static class BloatedPojoBuilder {
        private Integer id = null;
        private String name = null;
        private int intValue = 0;
        private double doubleValue = 0.0d;
        private java.sql.Date sqlDateValue = null;
        private java.sql.Timestamp sqlTimestampValue = null;
        private Clob clob = null;
        private InputStream inputStream = null;

        private BloatedPojoBuilder() {}

        public BloatedPojoBuilder id(Integer id) { this.id = id; return this; }
        public BloatedPojoBuilder name(String name) { this.name = name; return this; }
        public BloatedPojoBuilder invValue(int intValue) { this.intValue = intValue; return this; }
        public BloatedPojoBuilder doubleValue(double doubleValue) { this.doubleValue = doubleValue; return this; }
        public BloatedPojoBuilder sqlDate(Long sqlDate) { return sqlDateValue(new java.sql.Date(sqlDate)); }
        public BloatedPojoBuilder sqlDateValue(java.sql.Date sqlDateValue) { this.sqlDateValue = sqlDateValue; return this; }
        public BloatedPojoBuilder sqlTimestamp(Long sqlTimestamp) { return sqlTimestampValue(new java.sql.Timestamp(sqlTimestamp)); }
        public BloatedPojoBuilder sqlTimestampValue(java.sql.Timestamp sqlTimestampValue) { this.sqlTimestampValue = sqlTimestampValue; return this; }
        public BloatedPojoBuilder clobString(String clobString) { return clob(createClobValue(clobString)); }
        public BloatedPojoBuilder clob(Clob clob) { this.clob = clob; return this; }
        public BloatedPojoBuilder inputStreamString(String inputStreamString) { return inputStream(createInputStreamValue(inputStreamString)); }
        public BloatedPojoBuilder inputStream(InputStream inputStream) { this.inputStream = inputStream; return this; }

        public BloatedPojo build() {
            BloatedPojo pojo = new BloatedPojo();
            if (this.id != null) { // yes, this if-statement is required
                pojo.setId(id);
            }
            pojo.setName(name);
            pojo.setIntValue(intValue);
            pojo.setDoubleValue(doubleValue);
            pojo.setSqlDateValue(sqlDateValue);
            pojo.setSqlTimestampValue(sqlTimestampValue);
            pojo.setClobValue(clob);
            pojo.setStreamValue(inputStream);
            return pojo;
        }

        private static InputStream createInputStreamValue(String value) {
            if (value == null) {
                return null;
            }
            return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
        }

        private static Clob createClobValue(String value) {
            if (value == null) {
                return null;
            }
            try {
                return new JDBCClob(value);
            }
            catch (SQLException e) {
                throw new RuntimeException("Unable create clob: " + e.getMessage(), e);
            }
        }
    }
}
