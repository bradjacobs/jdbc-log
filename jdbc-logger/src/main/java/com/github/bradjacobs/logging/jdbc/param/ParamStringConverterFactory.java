package com.github.bradjacobs.logging.jdbc.param;

import com.github.bradjacobs.logging.jdbc.DatabaseType;

import java.time.ZoneId;

public class ParamStringConverterFactory
{
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");

    public static ParamToStringConverter getParamConverter(DatabaseType dbType) {
        return getParamConverter(dbType, DEFAULT_ZONE);
    }

    public static ParamToStringConverter getParamConverter(DatabaseType dbType, ZoneId zoneId)
    {
        if (dbType == null) {
            dbType = DatabaseType.UNKNOWN;
        }
        if (zoneId == null) {
            zoneId = DEFAULT_ZONE;
        }

        if (dbType == DatabaseType.ORACLE) {
            return new OracleParamToStringConverter(zoneId);
        }
        return new DefaultParamToStringConverter(zoneId);
    }
}
