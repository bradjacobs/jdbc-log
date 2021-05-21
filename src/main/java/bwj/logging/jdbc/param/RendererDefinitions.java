package bwj.logging.jdbc.param;

import java.util.Date;

public class RendererDefinitions
{
    private SqlParamRenderer<Object> defaultRenderer = null;
    private SqlParamRenderer<Boolean> boooleanRenderer = null;
    private SqlParamRenderer<String> stringRenderer = null;
    private SqlParamRenderer<Date> timestampRenderer = null;
    private SqlParamRenderer<Date> dateRenderer = null;
    private SqlParamRenderer<Date> timeRenderer = null;

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

    public void setAllTimeDateRenderers(SqlParamRenderer<Date> renderer) {
        setTimestampRenderer(renderer);
        setDateRenderer(renderer);
        setTimeRenderer(renderer);
    }

}
