package com.github.bradjacobs.logging.jdbc.param;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class OracleParamToStringConverter extends DefaultParamToStringConverter implements ParamToStringConverter {
    // oracle-specific date string constants
    private static final String ORACLE_TOTIMESTAMP_PREFIX = "TO_TIMESTAMP(";
    private static final String ORACLE_TOTIMESTAMP_SUFFIX = ", 'YYYY-MM-DD HH24:MI:SS')";
    private static final String ORACLE_TODATE_PREFIX = "TO_DATE(";
    private static final String ORACLE_TODATE_SUFFIX = ", 'YYYY-MM-DD')";

    public OracleParamToStringConverter() {
        super();
    }

    public OracleParamToStringConverter(ZoneId zoneId) {
        super(zoneId);
    }

    @Override
    protected String convertDate(DateTimeFormatter formatter, Date dateValue) {
        String dateString = super.convertDate(formatter, dateValue);

        String oraclePrefix = ORACLE_TOTIMESTAMP_PREFIX;
        String oracleSuffix = ORACLE_TOTIMESTAMP_SUFFIX;
        if ((dateValue instanceof java.sql.Date) || (dateValue instanceof java.sql.Time)) {
            oraclePrefix = ORACLE_TODATE_PREFIX;
            oracleSuffix = ORACLE_TODATE_SUFFIX;
        }
        return oraclePrefix + dateString + oracleSuffix;
    }
}
