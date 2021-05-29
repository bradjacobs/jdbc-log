package com.github.bradjacobs.logging.jdbc.param;

import java.util.Date;

public class RendererDefinitions
{
    private SqlParamRenderer<Object> defaultRenderer = null;
    private SqlParamRenderer<Boolean> boooleanRenderer = null;
    private SqlParamRenderer<String> stringRenderer = null;
    private SqlParamRenderer<Date> timestampRenderer = null;
    private SqlParamRenderer<Date> dateRenderer = null;
    private SqlParamRenderer<Date> timeRenderer = null;


    // dev note:  the following article was persuasion to avoid trying to use a "Class"
    //   as a key in some kind fo map lookup:  (might only apply to older Java versions but cannot say w/ certainty)
    //   http://frankkieviet.blogspot.com/2006/10/classloader-leaks-dreaded-permgen-space.html
    //   https://stackoverflow.com/questions/2625546/is-using-the-class-instance-as-a-map-key-a-best-practice



    public SqlParamRenderer<Object> getDefaultRenderer() {
        return defaultRenderer;
    }

    public void setDefaultRenderer(SqlParamRenderer<Object> defaultRenderer) {
        this.defaultRenderer = defaultRenderer;
    }

    public SqlParamRenderer<Boolean> getBoooleanRenderer() {
        return boooleanRenderer;
    }

    public void setBoooleanRenderer(SqlParamRenderer<Boolean> boooleanRenderer) {
        this.boooleanRenderer = boooleanRenderer;
    }

    public SqlParamRenderer<String> getStringRenderer() {
        return stringRenderer;
    }

    public void setStringRenderer(SqlParamRenderer<String> stringRenderer) {
        this.stringRenderer = stringRenderer;
    }

    public SqlParamRenderer<Date> getTimestampRenderer() {
        return timestampRenderer;
    }

    public void setTimestampRenderer(SqlParamRenderer<Date> timestampRenderer) {
        this.timestampRenderer = timestampRenderer;
    }

    public SqlParamRenderer<Date> getDateRenderer() {
        return dateRenderer;
    }

    public void setDateRenderer(SqlParamRenderer<Date> dateRenderer) {
        this.dateRenderer = dateRenderer;
    }

    public SqlParamRenderer<Date> getTimeRenderer() {
        return timeRenderer;
    }

    public void setTimeRenderer(SqlParamRenderer<Date> timeRenderer) {
        this.timeRenderer = timeRenderer;
    }

    public boolean hasNullRenderers()
    {
        if (defaultRenderer == null || boooleanRenderer == null ||
            stringRenderer == null || timestampRenderer == null ||
            dateRenderer == null || timeRenderer == null)
        {
            return true;
        }
        return false;
    }


    /**
     * Merges all the __NON-NULL__ values from the given defintions into the current object.
     * @param other the RendererDefintions to merge into this object
     */
    public void mergeIn(RendererDefinitions other)
    {
        if (other.getDefaultRenderer() != null) {
            this.setDefaultRenderer(other.getDefaultRenderer());
        }
        if (other.getBoooleanRenderer() != null) {
            this.setBoooleanRenderer(other.getBoooleanRenderer());
        }
        if (other.getStringRenderer() != null) {
            this.setStringRenderer(other.getStringRenderer());
        }
        if (other.getTimestampRenderer() != null) {
            this.setTimestampRenderer(other.getTimestampRenderer());
        }
        if (other.getDateRenderer() != null) {
            this.setDateRenderer(other.getDateRenderer());
        }
        if (other.getTimeRenderer() != null) {
            this.setTimestampRenderer(other.getTimeRenderer());
        }
    }



}
