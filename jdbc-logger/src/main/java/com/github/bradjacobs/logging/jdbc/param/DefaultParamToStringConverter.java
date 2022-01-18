package com.github.bradjacobs.logging.jdbc.param;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DefaultParamToStringConverter implements ParamToStringConverter
{
    protected static final ZoneId DEFAULT_ZONE = ParamStringConverterFactory.DEFAULT_ZONE;
    protected static final String DEFAULT_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";
    protected static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    protected static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";

    protected final DateTimeFormatter timestampFormatter;
    protected final DateTimeFormatter dateFormatter;
    protected final DateTimeFormatter timeFormatter;
    protected final ZoneId zoneId;

    public DefaultParamToStringConverter() {
        this(ParamStringConverterFactory.DEFAULT_ZONE);
    }

    public DefaultParamToStringConverter(ZoneId zoneId) {
        this.zoneId = (zoneId != null ? zoneId : DEFAULT_ZONE);
        this.timestampFormatter = DateTimeFormatter.ofPattern(DEFAULT_TIMESTAMP_PATTERN).withZone(zoneId);
        this.dateFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN).withZone(zoneId);
        this.timeFormatter = DateTimeFormatter.ofPattern(DEFAULT_TIME_PATTERN).withZone(zoneId);
    }

    public String convertToString(Object paramValue)
    {
        if (paramValue == null) {
            return convertNull();
        }
        else if (paramValue instanceof String) {
            return convertString((String)paramValue);
        }
        else if (paramValue instanceof Boolean) {
            return convertBoolean((Boolean)paramValue);
        }
        else if (paramValue instanceof Number) {
            return convertNumber((Number)paramValue);
        }
        else if (paramValue instanceof java.sql.Date) {
            return convertDate(dateFormatter, (Date)paramValue);
        }
        else if (paramValue instanceof java.sql.Time) {
            return convertDate(timeFormatter, (Date)paramValue);
        }
        else if (paramValue instanceof java.util.Date) {
            // includes both java.sql.Timestamp and java.util.Date
            return convertDate(timestampFormatter, (Date)paramValue);
        }
        else {
            return convertDefault(paramValue);
        }
    }

    protected String convertNull() {
        return "null";
    }

    protected String convertString(String stringValue) {
        if (stringValue.contains("'")) {
            stringValue = StringUtils.replace(stringValue, "'", "''");
        }
        return "'" + stringValue + "'";
    }

    protected String convertBoolean(Boolean booleanValue) {
        return Boolean.TRUE.equals(booleanValue) ? "1" : "0";
    }

    protected String convertNumber(Number numberValue) {
        String numberString = numberValue.toString();
        if (numberString.contains("E")) {
            // if the string value number contains 'E', then it is in scientific notation,
            //   thus regenerate with BigDecimal to make normal looking number.
            numberString = BigDecimal.valueOf((numberValue).doubleValue()).toPlainString();
        }
        return numberString;
    }

    protected String convertDate(DateTimeFormatter formatter, Date dateValue) {
        return "'" + formatter.format(Instant.ofEpochMilli(dateValue.getTime())) + "'";
    }

    protected String convertDefault(Object objectValue) {
        return String.valueOf(objectValue);
    }



    // NOTE about java.sql.Array  (Not yet Implemented)
    //
    //     else if (paramValue instanceof java.sql.Array) {
    //            java.sql.Array paramArray = (java.sql.Array)paramValue;
    //            Object[] values = (Object[])paramArray.getArray()
    //            ... convert the values to a string ...
    //    }
    // note the 'paramArray.getArray()' can throw a SqlException,
    //   which would mean that converting the values to a string _might_ happen
    //   in a different location.
}
