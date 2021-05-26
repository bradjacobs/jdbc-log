package bwj.logging.jdbc.param;


import java.util.Map;

public class TagFiller
{
    private static final String DEFAULT_TAG = "?";

    private final String tag;
    private final RendererSelector rendererSelector;

    public TagFiller(RendererSelector rendererSelector)
    {
        this(DEFAULT_TAG, rendererSelector);
    }

    public TagFiller(String tag, RendererSelector rendererSelector)
    {
        validateParams(tag, rendererSelector);
        this.tag = tag;
        this.rendererSelector = rendererSelector;
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


    public void appendValue(Object object, StringBuilder sb)
    {
        SqlParamRenderer paramRenderer = rendererSelector.getRenderer(object);

        paramRenderer.appendParamValue(object, sb);
    }


    private void validateParams(String tag, RendererSelector rendererSelector) throws IllegalArgumentException
    {
        if (tag == null || tag.isEmpty()) {
            throw new IllegalArgumentException("Must provide a tag parameter.");
        }
        if (rendererSelector == null) {
            throw new IllegalArgumentException("Must provide a rendererSelector.");
        }
    }

}
