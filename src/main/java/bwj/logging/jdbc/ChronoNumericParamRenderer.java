package bwj.logging.jdbc;

import java.util.Date;

public class ChronoNumericParamRenderer<T extends Date> implements ChronoParamRenderer<T>
{
    public ChronoNumericParamRenderer()
    {
    }

    @Override
    public void appendParamValue(T value, StringBuilder sb)
    {
        sb.append(value.getTime());
    }
}
