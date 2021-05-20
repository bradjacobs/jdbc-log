package bwj.logging.jdbc;

import org.apache.commons.lang.StringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ChronoStringParamRenderer<T extends Date> implements ChronoParamRenderer<T>
{
    private static final ZoneId DEFAULT_ZONE = DefaultSqlParamRendererFactory.DEFAULT_ZONE;
    protected final DateTimeFormatter formatter;

    public ChronoStringParamRenderer(String pattern, ZoneId zoneId)
    {
        if (StringUtils.isEmpty(pattern)) {
            throw new IllegalArgumentException("datetime formatter pattern is required.");
        }
        if (zoneId == null) {
            zoneId = DEFAULT_ZONE;
        }
        this.formatter = DateTimeFormatter.ofPattern(pattern).withZone(zoneId);
    }

    @Override
    public void appendParamValue(T value, StringBuilder sb)
    {
        sb.append('\'');
        sb.append(formatter.format(Instant.ofEpochMilli(value.getTime())));
        sb.append('\'');
    }
}
