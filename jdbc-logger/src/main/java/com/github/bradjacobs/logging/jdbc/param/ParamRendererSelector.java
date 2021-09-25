package com.github.bradjacobs.logging.jdbc.param;

public class ParamRendererSelector
{
    private final RendererDefinitions rendererDefinitions;

    public ParamRendererSelector(RendererDefinitions rendererDefinitions)
    {
        if (rendererDefinitions == null) {
            throw new IllegalArgumentException("Must provide rendererDefinitions.");
        }
        else if (rendererDefinitions.hasNullRenderers()) {
            throw new IllegalArgumentException("RendererDefinitions must have all values set.");
        }
        this.rendererDefinitions = rendererDefinitions;
    }



    /*
        NOTES for method below:
          1. It would 'probably' be ok to have done something like (String.class.equals(objValue.getClass()),
             but uncertain of some jdbc have special subtypes of classes, so keeping the instanceof usage for now.
          2. Most likely isAssignableFrom vs instanceOf wouldn't matter for this situation but has not been confirmed
          3. Argument could be made to have a Map<Class,SqlParamRenderer>, but having a class as a 'map key'
             in older versions of Java could cause memory leak issues.  This is 'probably' no longer the case,
             but certainty isn't high enough to risk that.  For details:
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
            rendererDefinitions.getStringRenderer().appendParamValue((String)objValue, sb);
        }
        else if (objValue instanceof Number) {
            rendererDefinitions.getNumberRenderer().appendParamValue((Number)objValue, sb);
        }
        else if (objValue instanceof Boolean) {
            rendererDefinitions.getBooleanRenderer().appendParamValue((Boolean)objValue, sb);
        }
        else if (objValue instanceof java.sql.Timestamp) {
            rendererDefinitions.getTimestampRenderer().appendParamValue((java.sql.Timestamp)objValue, sb);
        }
        else if (objValue instanceof java.sql.Date) {
            rendererDefinitions.getDateRenderer().appendParamValue((java.sql.Date)objValue, sb);
        }
        else if (objValue instanceof java.sql.Time) {
            rendererDefinitions.getTimeRenderer().appendParamValue((java.sql.Time)objValue, sb);
        }
        else if (objValue instanceof java.util.Date) {
            // it is technically possible that the JDBC can get a java.util.Date (instead of a java.sql.Date)
            //    e.g.  setObject(x, x)
            // if this occurs render it the same as timestamp (i.e.  date + time)
            rendererDefinitions.getTimestampRenderer().appendParamValue((java.util.Date)objValue, sb);
        }
        else {
            rendererDefinitions.getDefaultRenderer().appendParamValue(objValue, sb);
        }
    }
}
