package org.hisp.dhis.lib.expression.ast;

import org.hisp.dhis.lib.expression.spi.ValueType;
import org.hisp.dhis.lib.expression.spi.VariableValue;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static java.lang.String.format;

public interface Typed {

    static Double toNumberTypeCoercion(Object value) {
        if (value == null) return null;
        if (value instanceof VariableValue v) return toNumberTypeCoercion(v.valueOrDefault());
        if (value instanceof Boolean) return value == Boolean.TRUE ? 1d : 0d;
        if (value instanceof Number n) return n.doubleValue();
        return Double.valueOf(value.toString());
    }

    static Boolean toBooleanTypeCoercion(Object value) {
        if (value == null) return null;
        if (value instanceof VariableValue v) return toBooleanTypeCoercion(v.valueOrDefault());
        if (value instanceof Boolean b) return b;
        if (value instanceof Number n) {
            if (!isNonFractionValue(n)) {
                throw new IllegalArgumentException(format("Could not coerce Double '%s' to Boolean", value));
            }
            return n.intValue() != 0;
        }
        return Boolean.valueOf(value.toString());
    }

    static LocalDate toDateTypeCoercion(Object value) {
        if (value == null) return null;
        if (value instanceof VariableValue v) return toDateTypeCoercion(v.valueOrDefault());
        if (value instanceof LocalDate d) return d;
        if (value instanceof String s) return LocalDate.parse(s);
        if (value instanceof Date d) return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        throw new IllegalArgumentException(format("Count not coerce to date: '%s'", value));
    }

    static String toStringTypeCoercion(Object value) {
        if (value == null) return null;
        if (value instanceof VariableValue v) return v.valueOrDefault().toString();
        return value.toString();
    }

    static Object toMixedTypeTypeCoercion(Object value) {
        if (value == null) return null;
        if (value instanceof VariableValue v) {
            return switch (v.valueType()) {
                case NUMBER -> toNumberTypeCoercion(v.valueOrDefault());
                case BOOLEAN -> toBooleanTypeCoercion(v.valueOrDefault());
                case DATE -> toDateTypeCoercion(v.valueOrDefault());
                default -> toStringTypeCoercion(v.valueOrDefault());
            };
        }
        return value;
    }

    static boolean isNonFractionValue(Number value) {
        return value.doubleValue() % 1.0d == 0d;
    }

    ValueType getValueType();
}
