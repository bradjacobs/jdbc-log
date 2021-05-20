package bwj.logging.jdbc;

import java.util.Date;

public class ChronoNumericParamRenderer implements SqlParamRenderer<Date>
{
    public ChronoNumericParamRenderer()
    {
    }

    @Override
    public void appendParamValue(Date value, StringBuilder sb)
    {
        sb.append(value.getTime());
    }
}
