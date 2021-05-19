package bwj.logging.jdbc;

public class DefaultRenderer implements Renderer
{
    public void render(Object object, StringBuilder sb)
    {
        if (object instanceof Class) {
            sb.append('\'').append(((Class) object).getName()).append('\'');
        }
        else if (object instanceof String || object instanceof java.util.Date) {
            sb.append('\'').append(object).append('\'');
        }
        else if (object instanceof Boolean) {
            sb.append((Boolean) object ? 1 : 0);
        }
        else {
            sb.append(object);
        }
    }
}
