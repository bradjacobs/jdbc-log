package bwj.logging.jdbc.param;

import org.apache.commons.lang.StringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

public class ChronoParamRendererFactory
{
    private static final String DEFAULT_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    private static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");

    private ChronoParamRendererFactory() { }

    /**
     * Will render any Timestamp/Date/Time object into a string using the given dateFormatter pattern.
     * @param pattern date/time pattern
     * @return renderer
     * @see DateTimeFormatterBuilder#appendPattern(String)
     */
    public static ChronoParamRenderer<Date> createChronoStringParamRenderer(String pattern) {
        return createChronoStringParamRenderer(pattern, DEFAULT_ZONE);
    }

     /**
     * Will render any Timestamp/Date/Time object into a string using the given dateFormatter pattern.
     * @param pattern date/time pattern
     * @return renderer
     * @see DateTimeFormatterBuilder#appendPattern(String)
     */

    /**
     * Will render any Timestamp/Date/Time object into a string using the given dateFormatter pattern.
     * @param pattern date/time pattern
     * @param zoneId optional timezone to use to generate date string pattern
     * @return renderer
     * @see DateTimeFormatterBuilder#appendPattern(String)
     */
    public static ChronoParamRenderer<Date> createChronoStringParamRenderer(String pattern, ZoneId zoneId) {
        return new ChronoStringParamRenderer<>(pattern, zoneId);
    }

    /**
     * Returns in the Timestamp/Date/Time being rendered in epoch milliseconds.
     * @return renderer
     */
    public static ChronoParamRenderer<Date> createChronoNumericParamRenderer() {
        return new ChronoNumericParamRenderer<>();
    }







    public static ChronoParamRenderer<Date> createDefaultTimestampParamRenderer() {
        return createDefaultTimestampParamRenderer(DEFAULT_ZONE);
    }
    public static ChronoParamRenderer<Date> createDefaultTimestampParamRenderer(ZoneId zoneId) {
        return createChronoStringParamRenderer(DEFAULT_TIMESTAMP_PATTERN, zoneId);
    }


    public static ChronoParamRenderer<Date> createDefaultDateParamRenderer() {
        return createDefaultDateParamRenderer(DEFAULT_ZONE);
    }
    public static ChronoParamRenderer<Date> createDefaultDateParamRenderer(ZoneId zoneId) {
        return createChronoStringParamRenderer(DEFAULT_DATE_PATTERN, zoneId);
    }


    public static ChronoParamRenderer<Date> createDefaultTimeParamRenderer() {
        return createDefaultTimeParamRenderer(DEFAULT_ZONE);
    }
    public static ChronoParamRenderer<Date> createDefaultTimeParamRenderer(ZoneId zoneId) {
        return createChronoStringParamRenderer(DEFAULT_TIME_PATTERN, zoneId);
    }















    static class ChronoStringParamRenderer<T extends Date> implements ChronoParamRenderer<T>
    {
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



    static class ChronoNumericParamRenderer<T extends Date> implements ChronoParamRenderer<T>
    {
        @Override
        public void appendParamValue(T value, StringBuilder sb) {
            sb.append(value.getTime());
        }
    }


}
