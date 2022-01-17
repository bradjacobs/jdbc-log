package com.github.bradjacobs.logging.jdbc.param;

import com.github.bradjacobs.logging.jdbc.DatabaseType;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ParamToStringConverter
{
    private static final String DEFAULT_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    private static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");

    // oracle-specific date string constants
    private static final String ORACLE_TOTIMESTAMP_PREFIX = "TO_TIMESTAMP(";
    private static final String ORACLE_TOTIMESTAMP_SUFFIX = ", 'YYYY-MM-DD HH24:MI:SS')";
    private static final String ORACLE_TODATE_PREFIX = "TO_DATE(";
    private static final String ORACLE_TODATE_SUFFIX = ", 'YYYY-MM-DD')";


    private DateTimeFormatter timestampFormatter;
    private DateTimeFormatter dateFormatter;
    private DateTimeFormatter timeFormatter;

    private ZoneId zoneId;
    private boolean renderDatesAsEpochUtc = false;
    private DatabaseType databaseType = DatabaseType.UNKNOWN;

    public ParamToStringConverter() {
        this(DEFAULT_ZONE);
    }
    public ParamToStringConverter(ZoneId zoneId) {
        this.zoneId = zoneId;
        setDateTimeFormatters(zoneId);
    }

    private void setDateTimeFormatters(ZoneId zoneId) {
        timestampFormatter = DateTimeFormatter.ofPattern(DEFAULT_TIMESTAMP_PATTERN).withZone(zoneId);
        dateFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN).withZone(zoneId);
        timeFormatter = DateTimeFormatter.ofPattern(DEFAULT_TIME_PATTERN).withZone(zoneId);
    }

    public void setZoneId(ZoneId zoneId) {
        if (zoneId == null) {
            zoneId = DEFAULT_ZONE;
        }
        this.zoneId = zoneId;
        setDateTimeFormatters(zoneId);
    }

    public void setDatabaseType(DatabaseType databaseType) {
        if (databaseType == null) {
            databaseType = DatabaseType.UNKNOWN;
        }
        this.databaseType = databaseType;
    }

    public void setRenderDatesAsEpochUtc(boolean renderDatesAsEpochUtc) {
        this.renderDatesAsEpochUtc = renderDatesAsEpochUtc;
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
        if (paramValue instanceof java.sql.Date) {
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

    private String makeDateString(DateTimeFormatter formatter, Date dateValue)
    {
        if (this.renderDatesAsEpochUtc) {
            return String.valueOf(dateValue.getTime());
        }
        String dateString = "'" + formatter.format(Instant.ofEpochMilli(dateValue.getTime())) + "'";

        if (this.databaseType.equals(DatabaseType.ORACLE)) {
            String oraclePrefix = ORACLE_TOTIMESTAMP_PREFIX;
            String oracleSuffix = ORACLE_TOTIMESTAMP_SUFFIX;
            if ((dateValue instanceof java.sql.Date) || (dateValue instanceof java.sql.Time)) {
                oraclePrefix = ORACLE_TODATE_PREFIX;
                oracleSuffix = ORACLE_TODATE_SUFFIX;
            }
            dateString = oraclePrefix + dateString + oracleSuffix;
        }

        return dateString;
    }
}
