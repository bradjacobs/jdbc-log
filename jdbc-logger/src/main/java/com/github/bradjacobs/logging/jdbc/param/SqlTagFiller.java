package com.github.bradjacobs.logging.jdbc.param;


import java.util.Map;

public class SqlTagFiller
{
    private static final String DEFAULT_TAG = "?";

    private final String tag;
    private final ParamToStringConverter paramToStringConverter;

    public SqlTagFiller(ParamToStringConverter paramToStringConverter)
    {
        this(DEFAULT_TAG, paramToStringConverter);
    }

    public SqlTagFiller(String tag, ParamToStringConverter paramToStringConverter)
    {
        this.tag = tag;
        this.paramToStringConverter = paramToStringConverter;
    }

    /**
     * Replaces the tags in the source SQL string with the given values in the paramMap
     * Example:
     *     source: select * from tbl where id = ? AND name = ?
     *     paramMap: {{1,1}, {2,"Bob"}}
     *     output: select * from tbl where id = 1 AND name = 'Bob'
     * @param source sql string with tags/question marks
     * @param paramMap parameter values
     * @return the 'filled in' SQL string.
     */
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
                sb.append( paramToStringConverter.convertToString(paramMap.get(tagNumber)) );
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
}
