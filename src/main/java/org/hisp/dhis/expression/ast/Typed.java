package org.hisp.dhis.expression.ast;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static java.lang.String.format;

public interface Typed {

    static Double toNumberTypeCoercion(Object value)
    {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return value == Boolean.TRUE ? 1d : 0d;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.valueOf(value.toString());
    }

    static Boolean toBooleanTypeCoercion(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            if (!isNonFractionValue((Number) value)) {
                throw new IllegalArgumentException(format("Could not coerce Double '%s' to Boolean", value));
            }
            return ((Number) value).intValue() != 0;
        }
        return Boolean.valueOf(value.toString());
    }

    static LocalDate toDateTypeCoercion(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate) return (LocalDate) value;
        if (value instanceof String) return LocalDate.parse((String) value);
        if (value instanceof Date) return LocalDate.ofInstant (((Date) value).toInstant(), ZoneId.systemDefault());
        throw new IllegalArgumentException(format("Count not coerce to date: '%s'", value));
    }

    static String toStringTypeCoercion(Object value) {
        return value == null ? null : value.toString();
    }

    static boolean isNonFractionValue(Number value) {
        return value.doubleValue() % 1.0d == 0d;
    }

    ValueType getValueType();
}
