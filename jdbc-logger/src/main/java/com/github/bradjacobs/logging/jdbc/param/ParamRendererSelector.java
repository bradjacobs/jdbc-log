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


    public SqlParamRenderer getRenderer(Object objValue) {

        if (objValue instanceof String) {
            return rendererDefinitions.getStringRenderer();
        }
        else if (objValue instanceof Boolean) {
            return rendererDefinitions.getBooleanRenderer();
        }
        else if (objValue instanceof java.sql.Timestamp) {
            return rendererDefinitions.getTimestampRenderer();
        }
        else if (objValue instanceof java.sql.Date) {
            return rendererDefinitions.getDateRenderer();
        }
        else if (objValue instanceof java.sql.Time) {
            return rendererDefinitions.getTimeRenderer();
        }
        else if (objValue instanceof java.util.Date) {
            // it is possible that the JDBC can get a java.util.Date (instead of a java.sql.Date)
            //    e.g.  setObject(x, x)
            // if this occurs render it the same as timestamp (i.e.  date + time)
            return rendererDefinitions.getTimestampRenderer();
        }
        else {
            return rendererDefinitions.getDefaultRenderer();
        }
    }

}
