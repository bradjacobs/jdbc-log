package com.github.bradjacobs.logging.jdbc.param;

public interface SqlParamRenderer<T>
{
    void appendParamValue(T value, StringBuilder sb);
}
