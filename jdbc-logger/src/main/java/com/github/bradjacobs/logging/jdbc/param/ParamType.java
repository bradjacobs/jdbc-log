package com.github.bradjacobs.logging.jdbc.param;

import java.util.Date;
import java.util.Objects;

/**
 * pseudo-enum with generic class parameter to help enforce type-safety.
 * @param <T>
 */
public class ParamType<T>
{
    public static final ParamType<String> STRING = new ParamType<>("string");
    public static final ParamType<Boolean> BOOLEAN = new ParamType<>("boolean");
    public static final ParamType<Number> NUMBER = new ParamType<>("number");
    public static final ParamType<Date> DATE = new ParamType<>("date");
    public static final ParamType<Date> TIME = new ParamType<>("time");
    public static final ParamType<Date> TIMESTAMP = new ParamType<>("timestamp");
    public static final ParamType<Object> DEFAULT = new ParamType<>("default");

    private final String name;

    public ParamType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParamType)) return false;
        ParamType<?> paramType = (ParamType<?>) o;
        return name.equals(paramType.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
