package com.github.bradjacobs.logging.jdbc.param.renderer;

import com.github.bradjacobs.logging.jdbc.param.SqlParamRenderer;
import org.apache.commons.lang3.StringUtils;

public class PrefixSuffixParamRenderer<T> implements SqlParamRenderer<T>
{
    private final SqlParamRenderer<T> innerRenderer;
    private final String prefix;
    private final String suffix;

    /**
     * 'Decorate' an existing param renderer with additional prefix and suffix strings.
     * @param renderer the original param renderer
     * @param prefix string value to prepend
     * @param suffix string value to append.
     * @return renderer
     */
    public PrefixSuffixParamRenderer(SqlParamRenderer<T> renderer, String prefix, String suffix)
    {
        if (renderer == null) {
            throw new IllegalArgumentException("Renderer parameter is required.");
        }
        this.innerRenderer = renderer;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Override
    public void appendParamValue(T value, StringBuilder sb)
    {
        if (StringUtils.isNotEmpty(prefix)) {
            sb.append(prefix);
        }
        innerRenderer.appendParamValue(value, sb);
        if (StringUtils.isNotEmpty(suffix)) {
            sb.append(suffix);
        }
    }
}