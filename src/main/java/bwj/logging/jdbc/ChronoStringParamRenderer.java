package bwj.logging.jdbc;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ChronoStringParamRenderer implements SqlParamRenderer<Date>
{
    protected final DateTimeFormatter formatter;

    public ChronoStringParamRenderer(String pattern, ZoneId zoneId)
    {
        this.formatter = DateTimeFormatter.ofPattern(pattern).withZone(zoneId);
    }

    @Override
    public void appendParamValue(Date value, StringBuilder sb)
    {
        sb.append('\'');
        sb.append(formatter.format(Instant.ofEpochMilli(value.getTime())));
        sb.append('\'');
    }
}
