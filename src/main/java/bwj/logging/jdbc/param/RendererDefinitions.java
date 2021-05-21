package bwj.logging.jdbc.param;

import java.util.Date;

public class RendererDefinitions
{
    private SqlParamRenderer<Object> defaultRenderer = null;
    private SqlParamRenderer<Boolean> boooleanRenderer = null;
    private SqlParamRenderer<String> stringRenderer = null;
    private SqlParamRenderer<?> timestampRenderer = null;
    private SqlParamRenderer<?> dateRenderer = null;
    private SqlParamRenderer<?> timeRenderer = null;

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

    public <T extends Date> SqlParamRenderer<T> getTimestampRenderer() {
        return (SqlParamRenderer<T>) timestampRenderer;
    }

    public <T extends Date> void setTimestampRenderer(SqlParamRenderer<T> timestampRenderer) {
        this.timestampRenderer = timestampRenderer;
    }

    public <T extends Date> SqlParamRenderer<T> getDateRenderer() {
        return (SqlParamRenderer<T>) dateRenderer;
    }

    public <T extends Date> void setDateRenderer(SqlParamRenderer<T> dateRenderer) {
        this.dateRenderer = dateRenderer;
    }

    public <T extends Date> SqlParamRenderer<T> getTimeRenderer() {
        return (SqlParamRenderer<T>) timeRenderer;
    }

    public <T extends Date> void setTimeRenderer(SqlParamRenderer<T> timeRenderer) {
        this.timeRenderer = timeRenderer;
    }

    public void setAllTimeDateRenderers(SqlParamRenderer<Date> renderer) {
        setTimestampRenderer(renderer);
        setDateRenderer(renderer);
        setTimeRenderer(renderer);
    }

}
