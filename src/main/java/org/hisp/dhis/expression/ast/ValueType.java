package org.hisp.dhis.expression.ast;

import java.time.LocalDate;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * A rough classification of what values building blocks expect and return.
 *
 * These can be understood as semantic value types.
 *
 * @author Jan Bernitt
 */
public enum ValueType {

    /**
     * Type can be at least two of the following: numbers, booleans, dates, strings, list/array of these.
     * This is also used in case a type is unknown or cannot be determined statically.
     */
    MIXED,

    NUMBER,

    BOOLEAN,

    DATE,

    STRING,

    /**
     * Means the type can be mixed but all SAME argument should be of the same actual type.
     * If the return type is also SAME it is the actual type of the SAME parameter type.
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
     *
     * This means the two are type compatible.
     * If {@link #MIXED} is involved this still might fail with certain actual values.
     *
     * @param other target type
     * @return true, if this type (actual) is assignable to the other (required), otherwise false
     */
    public boolean isAssignableTo(ValueType other) {
        return !isSame() && (this == other || isMixed() || other.isMixed() || other.isSame());
    }

    /**
     * <b>May</b> a value of this <b>potentially</b> be usable for the other type.
     *
     * This means a value potentially can be converted to the required type using a known conversion.
     * Such conversion might fail because the input did not have the required format.
     *
     * @param other target type
     * @return true, if this type (actual) is assignable or convertable to the other (required), otherwise false
     */
    public boolean isTypeCoercionTo(ValueType other) {
        if (isAssignableTo(other)) return true;
        switch (other) {
            case STRING: return true;
            case NUMBER: return this == STRING || this == BOOLEAN;
            case DATE: return this == STRING;
            case BOOLEAN: return this == STRING || this == NUMBER;
            default: return false;
        }
    }

    public boolean isValidValue(Object value) {
        switch (this) {
            case STRING: return value instanceof String;
            case NUMBER: return value instanceof Number;
            case BOOLEAN: return value instanceof Boolean;
            case DATE: return value instanceof LocalDate || value instanceof Date;
            default: return true;
        }
    }

    public static boolean allSame(List<ValueType> types) {
        EnumSet<ValueType> present = EnumSet.noneOf(ValueType.class);
        for (ValueType t : types)
            if (t != MIXED) present.add(t);
        return present.size() <= 1;
    }
}
