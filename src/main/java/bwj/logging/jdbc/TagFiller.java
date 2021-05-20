package bwj.logging.jdbc;

import java.util.Collections;
import java.util.Map;

public class TagFiller
{
    private static final String DEFAULT_TAG = "?";
    private static final SqlParamRenderer DEFAULT_PARAM_RENDERER = new BasicParamRenderer();

    private final String tag;
    private final Map<Class, SqlParamRenderer> paramRenderMap;

    public TagFiller(Map<Class, SqlParamRenderer> paramRenderMap) {
        this(DEFAULT_TAG, paramRenderMap);
    }

    public TagFiller(String tag, Map<Class, SqlParamRenderer> paramRenderMap)
    {
        if (tag == null || tag.isEmpty()) {
            throw new IllegalArgumentException("Must provide a tag parameter.");
        }
        this.tag = tag;

        if (paramRenderMap == null) {
            paramRenderMap = Collections.emptyMap();
        }
        this.paramRenderMap = paramRenderMap;
    }

    public String replace(String source, Map<Integer, Object> paramMap)
    {
        if (source == null) {
            return null;
        }

        int tagIdx = source.indexOf(tag);

        // if there's nothing to replace, then return the original source.
        if (tagIdx < 0 || paramMap == null || paramMap.isEmpty()) {
            return source;
        }

        int lastIdx = 0;
        StringBuilder sb = new StringBuilder(source.length());
        int tagNumber = 1;

        while (tagIdx >= 0)
        {
            sb.append(source, lastIdx, tagIdx);

            // distinguish b/w having a null value for a given key vs there's a missing entry in the map
            if (paramMap.containsKey(tagNumber)) {
                appendValue(paramMap.get(tagNumber), sb);
            }
            else {
                // specific entry missing in param map, thus just leave existing tag
                sb.append(tag);
            }

            lastIdx = tagIdx + 1;
            tagIdx = source.indexOf(tag, lastIdx);
            tagNumber++;
        }
        sb.append(source.substring(lastIdx));

        return sb.toString();
    }

    //  SQL uses a very specific date format, which is 'YYYY-MM-DD'.
    //  YYYY-MM-DD HH:MM:SS

    public void appendValue(Object object, StringBuilder sb)
    {
        SqlParamRenderer paramRenderer = null;
        if (object != null) {
            paramRenderer = paramRenderMap.get(object.getClass());
        }

        if (paramRenderer == null) {
            paramRenderer = DEFAULT_PARAM_RENDERER;
        }

        // todo - fix type checking
        paramRenderer.appendParamValue(object, sb);

    }




    private static class BasicParamRenderer implements SqlParamRenderer {
        @Override
        public void appendParamValue(Object value, StringBuilder sb)
        {
            sb.append(value);
        }
    }
}
