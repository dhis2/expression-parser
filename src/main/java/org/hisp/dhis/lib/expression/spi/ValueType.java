package org.hisp.dhis.lib.expression.spi;

import java.time.LocalDate;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

/**
 * A rough classification of what values building blocks expect and return.
 * <p>
 * These can be understood as semantic value types.
 *
 * @author Jan Bernitt
 */
public enum ValueType {

    /**
     * Type can be at least two of the following: numbers, booleans, dates, strings, list/array of these. This is also
     * used in case a type is unknown or cannot be determined statically.
     */
    MIXED,

    NUMBER,

    BOOLEAN,

    DATE,

    STRING,

    /**
     * Means the type can be mixed but all SAME argument should be of the same actual type. If the return type is also
     * SAME it is the actual type of the SAME parameter type.
     */
    SAME;

    public boolean isSame() {
        return this == SAME;
    }

    public boolean isMixed() {
        return this == MIXED;
    }

    /**
     * <b>Can</b> a value of this type <b>definitely</b> be used for the other type.
     * <p>
     * This means the two are type compatible. If {@link #MIXED} is involved this still might fail with certain actual
     * values.
     *
     * @param other target type
     * @return true, if this type (actual) is assignable to the other (required), otherwise false
     */
    public boolean isAssignableTo(ValueType other) {
        return !isSame() && (this == other || isMixed() || other.isMixed() || other.isSame()
                || this == STRING && other == DATE); // there are no date literals, so strings are used
    }

    /**
     * <b>May</b> a value of this <b>potentially</b> be usable for the other type.
     * <p>
     * This means a value potentially can be converted to the required type using a known conversion. Such conversion
     * might fail because the input did not have the required format.
     *
     * @param other target type
     * @return true, if this type (actual) is assignable or convertable to the other (required), otherwise false
     */
    public boolean isMaybeAssignableTo(ValueType other) {
        if (isAssignableTo(other)) return true;
        return switch (other) {
            case STRING -> true;
            case NUMBER -> this == STRING || this == BOOLEAN;
            case DATE -> this == STRING;
            case BOOLEAN -> this == STRING || this == NUMBER;
            default -> false;
        };
    }

    public boolean isValidValue(Object value) {
        return switch (this) {
            case STRING -> value instanceof String;
            case NUMBER -> value instanceof Number;
            case BOOLEAN -> value instanceof Boolean;
            case DATE -> value instanceof LocalDate || value instanceof Date;
            default -> true;
        };
    }

    public static boolean allSame(List<ValueType> types) {
        EnumSet<ValueType> present = EnumSet.noneOf(ValueType.class);
        for (ValueType t : types)
            if (t != MIXED) present.add(t);
        return present.size() <= 1;
    }
}
