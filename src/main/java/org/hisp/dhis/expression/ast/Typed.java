package org.hisp.dhis.expression.ast;

import org.hisp.dhis.expression.spi.IllegalExpressionException;

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

    static String toStringTypeCoercion(Object value) {
        return value == null ? null : value.toString();
    }

    static boolean isNonFractionValue(Number value) {
        return value.doubleValue() % 1.0d == 0d;
    }

    ValueType getValueType();
}
