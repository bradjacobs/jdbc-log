package bwj.logging.jdbc.param;

public class RendererSelector
{
    private final RendererDefinitions rendererDefinitions;

    public RendererSelector(RendererDefinitions rendererDefinitions)
    {
        this.rendererDefinitions = rendererDefinitions;
    }


    public SqlParamRenderer getRenderer(Object objValue) {
        if (objValue == null) {
            return rendererDefinitions.getDefaultRenderer();
        }

        if (objValue instanceof String) {
            return rendererDefinitions.getStringRenderer();
        }
        else if (objValue instanceof Boolean) {
            return rendererDefinitions.getBoooleanRenderer();
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
        else {
            return rendererDefinitions.getDefaultRenderer();
        }
    }

}
