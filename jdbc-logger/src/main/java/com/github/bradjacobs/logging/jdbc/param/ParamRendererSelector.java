package com.github.bradjacobs.logging.jdbc.param;

import com.github.bradjacobs.logging.jdbc.DatabaseType;

import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

public class ParamRendererSelector
{
    private final SqlParamRenderer<Object> defaultRenderer;
    private final SqlParamRenderer<Boolean> booleanRenderer;
    private final SqlParamRenderer<String> stringRenderer;
    private final SqlParamRenderer<Number> numberRenderer;
    private final SqlParamRenderer<Date> timestampRenderer;
    private final SqlParamRenderer<Date> dateRenderer;
    private final SqlParamRenderer<Date> timeRenderer;

    public ParamRendererSelector() {
        this(null, null, null);
    }

    public ParamRendererSelector(DatabaseType dbType, ZoneId zoneId, Map<ParamType<Date>, SqlParamRenderer<Date>> dateTimeOverrideMap)
    {
        if (dbType == null) {
            dbType = DatabaseType.UNKNOWN;
        }
        if (zoneId == null) {
            zoneId = ParamRendererFactory.DEFAULT_ZONE;
        }
        if (dateTimeOverrideMap == null) {
            dateTimeOverrideMap = Collections.emptyMap();
        }

        ParamRendererFactory factory = ParamRendererFactory.builder().withDatabaseType(dbType).withZoneId(zoneId).build();

        this.defaultRenderer = factory.getRenderer(ParamType.DEFAULT);
        this.booleanRenderer = factory.getRenderer(ParamType.BOOLEAN);
        this.stringRenderer = factory.getRenderer(ParamType.STRING);
        this.numberRenderer = factory.getRenderer(ParamType.NUMBER);
        this.timestampRenderer = dateTimeOverrideMap.getOrDefault(ParamType.TIMESTAMP, factory.getRenderer(ParamType.TIMESTAMP));
        this.dateRenderer = dateTimeOverrideMap.getOrDefault(ParamType.DATE, factory.getRenderer(ParamType.DATE));
        this.timeRenderer = dateTimeOverrideMap.getOrDefault(ParamType.TIME, factory.getRenderer(ParamType.TIME));
    }

    /*
        NOTES for method below:
          1. It would 'probably' be ok to have done something like (String.class.equals(objValue.getClass()),
             but uncertain if some jdbc have special subtypes of classes, so keeping the instanceof usage for now.
          2. Most likely isAssignableFrom vs instanceOf wouldn't matter for this situation but has not been confirmed
          3. Argument could be made to have a Map<Class,SqlParamRenderer>, but having a class as a 'map key'
             in older versions of Java could cause memory leak issues.  This is 'probably' no longer the case,
             but certainty isn't high enough to risk it.  For details:
               a. http://frankkieviet.blogspot.com/2006/10/classloader-leaks-dreaded-permgen-space.html
               b. https://stackoverflow.com/questions/2625546/is-using-the-class-instance-as-a-map-key-a-best-practice
          4. Newer versions of JDK allow for 'pattern matching'  (i.e. "objValue instanceof String s")
             which would avoid some ugliness of extra cast, but leaving to be JDK 1.8 compliant.
     */
    /**
     * append the param value to the StringBuilder, based upon its object type
     * @param objValue param value object
     * @param sb StringBuilder to append to
     */
    public void appendParamValue(Object objValue, StringBuilder sb) {

        if (objValue instanceof String) {
            stringRenderer.appendParamValue((String)objValue, sb);
        }
        else if (objValue instanceof Number) {
            numberRenderer.appendParamValue((Number)objValue, sb);
        }
        else if (objValue instanceof Boolean) {
            booleanRenderer.appendParamValue((Boolean)objValue, sb);
        }
        else if (objValue instanceof java.sql.Timestamp) {
            timestampRenderer.appendParamValue((java.sql.Timestamp)objValue, sb);
        }
        else if (objValue instanceof java.sql.Date) {
            dateRenderer.appendParamValue((java.sql.Date)objValue, sb);
        }
        else if (objValue instanceof java.sql.Time) {
            timeRenderer.appendParamValue((java.sql.Time)objValue, sb);
        }
        else if (objValue instanceof java.util.Date) {
            // it is technically possible that the JDBC can get a java.util.Date (instead of a java.sql.Date)
            //    e.g.  setObject(x, x)
            // if this occurs render it the same as timestamp (i.e.  date + time)
            timestampRenderer.appendParamValue((java.util.Date)objValue, sb);
        }
        else {
            defaultRenderer.appendParamValue(objValue, sb);
        }
    }
}
