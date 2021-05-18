package bwj.logging.jdbc;

import java.util.Iterator;

public abstract class TagUpdater
{
    private static final Renderer defaultRenderer = new DefaultRenderer();

    public static class DefaultRenderer implements Renderer {
        public void render(Object object, StringBuilder sb) {
            sb.append(object);
        }
    }

    public static String replace(String source, String tag, Iterator<?> values) {
        return replace(source, tag, values, defaultRenderer);
    }

    public static String replace(String source, String tag, Iterator<?> values, Renderer renderer) {
        StringBuilder sb = new StringBuilder(source.length());
        int lastIdx = 0;
        int tagIdx = source.indexOf(tag);
        for (; tagIdx >= 0 && values.hasNext();
            lastIdx = tagIdx + 1, tagIdx = source.indexOf(tag, lastIdx)) {
            sb.append(source.substring(lastIdx, tagIdx));
            renderer.render(values.next(), sb);
        }
        sb.append(source.substring(lastIdx));
        return sb.toString();
    }

    public static interface Renderer {
        void render(Object param1Object, StringBuilder param1StringBuilder);
    }
}
