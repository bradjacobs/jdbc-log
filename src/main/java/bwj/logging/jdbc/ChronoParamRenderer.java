package bwj.logging.jdbc;

import java.util.Date;

/**
 * Interface for Date or any of it's subtypes ...
 * @see java.util.Date
 * @see java.sql.Timestamp
 * @see java.sql.Date
 * @see java.sql.Time
 *
 */
public interface ChronoParamRenderer<T extends Date> extends SqlParamRenderer<T>
{
    void appendParamValue(T value, StringBuilder sb);
}
