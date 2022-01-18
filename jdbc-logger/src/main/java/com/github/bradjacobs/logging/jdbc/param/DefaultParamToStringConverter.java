package com.github.bradjacobs.logging.jdbc.param;

import com.github.bradjacobs.logging.jdbc.DatabaseType;
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
            return "null";
        }
        else if (paramValue instanceof String) {
            String paramValueString = (String)paramValue;
            if (paramValueString.contains("'")) {
                paramValueString = StringUtils.replace(paramValueString, "'", "''");
            }
            return "'" + paramValueString + "'";
        }
        else if (paramValue instanceof Boolean) {
            return ((Boolean)paramValue) ? "1" : "0";
        }
        else if (paramValue instanceof Number) {
            String numberString = paramValue.toString();
            if (numberString.contains("E")) {
                // if the string value number contains 'E', then it's scientific notation,
                //   and will regenerate with BigDecimal to make normal looking number.
                numberString = BigDecimal.valueOf(((Number)paramValue).doubleValue()).toPlainString();
            }
            return numberString;
        }
        else if (paramValue instanceof java.sql.Date) {
            return makeDateString(dateFormatter, (Date)paramValue);
        }
        else if (paramValue instanceof java.sql.Time) {
            return makeDateString(timeFormatter, (Date)paramValue);
        }
        else if (paramValue instanceof java.util.Date) {
            // includes both java.sql.Timestamp and java.util.Date
            return makeDateString(timestampFormatter, (Date)paramValue);
        }
        else {
            return paramValue.toString();
        }
    }

    protected String makeDateString(DateTimeFormatter formatter, Date dateValue)
    {
        return "'" + formatter.format(Instant.ofEpochMilli(dateValue.getTime())) + "'";
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
