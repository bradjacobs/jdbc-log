package bwj.logging.jdbc;

public interface SqlParamRenderer<T>
{
    void appendParamValue(T value, StringBuilder sb);
}
