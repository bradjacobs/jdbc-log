package com.github.bradjacobs.logging.jdbc.param;

import com.github.bradjacobs.logging.jdbc.DatabaseType;
import com.github.bradjacobs.logging.jdbc.param.renderer.BasicParamRenderer;
import com.github.bradjacobs.logging.jdbc.param.renderer.BooleanParamRenderer;
import com.github.bradjacobs.logging.jdbc.param.renderer.ChronoStringParamRenderer;
import com.github.bradjacobs.logging.jdbc.param.renderer.NumberParamRenderer;
import com.github.bradjacobs.logging.jdbc.param.renderer.PrefixSuffixParamRenderer;
import com.github.bradjacobs.logging.jdbc.param.renderer.StringParamRenderer;

import java.time.ZoneId;
import java.util.Date;

// TODO - probably refactor & remove b/c this is slightly inverse squirrelly
public class ParamRendererFactory
{
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");

    private static final String DEFAULT_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    private static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";

    // oracle-specific date string constants
    private static final String ORACLE_TOTIMESTAMP_PREFIX = "TO_TIMESTAMP(";
    private static final String ORACLE_TODATE_PREFIX = "TO_DATE(";
    private static final String ORACLE_DATETIME_SUFFIX = ", 'YYYY-MM-DD HH24:MI:SS')";

    private static ParamRendererFactory defaultInstance = null;

    public static ParamRendererFactory getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = builder().build();
        }
        return defaultInstance;
    }

    private final DatabaseType dbType;
    private final ZoneId zoneId;

    private ParamRendererFactory(DatabaseType dbType, ZoneId zoneId) {
        this.dbType = (dbType != null ? dbType : DatabaseType.UNKNOWN);
        this.zoneId = (zoneId != null ? zoneId : DEFAULT_ZONE);
    }


    public static ParamRendererFactory.Builder builder() {
        return new ParamRendererFactory.Builder();
    }

    public static class Builder {
        private DatabaseType dbType = null;
        private ZoneId zoneId = null;

        public Builder withDatabaseType(DatabaseType dbType) {
            this.dbType = dbType;
            return this;
        }
        public Builder withZoneId(ZoneId zoneId) {
            this.zoneId = zoneId;
            return this;
        }

        public ParamRendererFactory build() {
            return new ParamRendererFactory(dbType, zoneId);
        }
    }


    public <T> SqlParamRenderer<T> getRenderer(ParamType<T> paramType) {

        if (paramType.equals(ParamType.STRING)) {
            return InternalTypeEnum.String.make(dbType, zoneId);
        }
        else if (paramType.equals(ParamType.BOOLEAN)) {
            return InternalTypeEnum.Boolean.make(dbType, zoneId);
        }
        else if (paramType.equals(ParamType.NUMBER)) {
            return InternalTypeEnum.Number.make(dbType, zoneId);
        }
        else if (paramType.equals(ParamType.DATE)) {
            return InternalTypeEnum.Date.make(dbType, zoneId);
        }
        else if (paramType.equals(ParamType.TIME)) {
            return InternalTypeEnum.Time.make(dbType, zoneId);
        }
        else if (paramType.equals(ParamType.TIMESTAMP)) {
            return InternalTypeEnum.TimeStamp.make(dbType, zoneId);
        }
        else {
            return InternalTypeEnum.Default.make(dbType, zoneId);
        }
    }


    /**
     * private enum for trickery to allow the generic type safety and to avoid weird casts.
     */
    private enum InternalTypeEnum {
        String {
            @Override
            @SuppressWarnings("unchecked")
            SqlParamRenderer<String> make(DatabaseType dbType, ZoneId zoneId) {
                return new StringParamRenderer();
            }
        },
        Boolean {
            @Override
            @SuppressWarnings("unchecked")
            SqlParamRenderer<Boolean> make(DatabaseType dbType, ZoneId zoneId) {
                return new BooleanParamRenderer();
            }
        },
        Number {
            @Override
            @SuppressWarnings("unchecked")
            SqlParamRenderer<Number> make(DatabaseType dbType, ZoneId zoneId) {
                return new NumberParamRenderer();
            }
        },
        Date {
            @Override
            @SuppressWarnings("unchecked")
            SqlParamRenderer<Date> make(DatabaseType dbType, ZoneId zoneId) {
                if (DatabaseType.ORACLE.equals(dbType)) {
                    // NOTE:  currently shows a full date/time (like timestamp)
                    //     argument could be made that this should only show 'date' portion, but error on the side of showing more info
                    return new PrefixSuffixParamRenderer<>(TimeStamp.make(null, zoneId), ORACLE_TODATE_PREFIX, ORACLE_DATETIME_SUFFIX);
                }
                return new ChronoStringParamRenderer(DEFAULT_DATE_PATTERN, zoneId);
            }
        },
        Time {
            @Override
            @SuppressWarnings("unchecked")
            SqlParamRenderer<Date> make(DatabaseType dbType, ZoneId zoneId) {
                if (DatabaseType.ORACLE.equals(dbType)) {
                    // NOTE: oracle really doesn't have 'time' so just assign the same as 'timestamp'
                    return new PrefixSuffixParamRenderer<>(TimeStamp.make(null, zoneId), ORACLE_TODATE_PREFIX, ORACLE_DATETIME_SUFFIX);
                }
                return new ChronoStringParamRenderer(DEFAULT_TIME_PATTERN, zoneId);
            }
        },
        TimeStamp {
            @Override
            @SuppressWarnings("unchecked")
            SqlParamRenderer<Date> make(DatabaseType dbType, ZoneId zoneId) {
                if (DatabaseType.ORACLE.equals(dbType)) {
                    //  NOTE: it is possible to have alternate w/ fractional second precision, but this is the 'default'
                    return new PrefixSuffixParamRenderer<>(TimeStamp.make(null, zoneId), ORACLE_TOTIMESTAMP_PREFIX, ORACLE_DATETIME_SUFFIX);
                }
                return new ChronoStringParamRenderer(DEFAULT_TIMESTAMP_PATTERN, zoneId);
            }
        },
        Default {
            @Override
            @SuppressWarnings("unchecked")
            SqlParamRenderer<Object> make(DatabaseType dbType, ZoneId zoneId) {
                return new BasicParamRenderer();
            }
        };

        abstract <T> SqlParamRenderer<T> make(DatabaseType dbType, ZoneId zoneId);
    }
}