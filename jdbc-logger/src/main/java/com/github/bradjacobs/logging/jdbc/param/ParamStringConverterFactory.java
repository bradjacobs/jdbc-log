package com.github.bradjacobs.logging.jdbc.param;

import com.github.bradjacobs.logging.jdbc.DatabaseType;
import com.github.bradjacobs.logging.jdbc.DbLoggingBuilder;

import java.time.ZoneId;

class ParamStringConverterFactory {

    private ParamStringConverterFactory() { }

    public static ParamToStringConverter getParamConverter(DatabaseType dbType) {
        return getParamConverter(dbType, null);
    }

    public static ParamToStringConverter getParamConverter(DatabaseType dbType, ZoneId zoneId) {
        dbType = (dbType != null ? dbType : DatabaseType.DEFAULT);
        zoneId = (zoneId != null ? zoneId : DbLoggingBuilder.DEFAULT_ZONE);

        if (dbType == DatabaseType.ORACLE) {
            return new OracleParamToStringConverter(zoneId);
        }
        return new DefaultParamToStringConverter(zoneId);
    }
}
