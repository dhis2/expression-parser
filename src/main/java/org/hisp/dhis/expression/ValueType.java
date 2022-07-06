package org.hisp.dhis.expression;

/**
 * A rough classification of what values building blocks expect and return.
 *
 * @author Jan Bernitt
 */
public enum ValueType {

    /**
     * The type is unknown or mixed.
     */
    UNKNOWN,

    /**
     * Means the type can be mixed but all SAME argument should be of the same actual type.
     * If the return type is also SAME it is the actual type of the SAME parameter type.
     */
    SAME,

    NUMBER,

    BOOLEAN,

    DATE,

    STRING;

    //TODO Set<ValueType> typeCoercionFrom; // set of types that can be made into the type

    public boolean isSame() {
        return this == SAME;
    }

    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    public boolean isAssignableTo(ValueType other) {
        return this == other || other.isUnknown() ||  other.isSame() || isUnknown() || isSame();
    }

    public boolean isPotentiallySameAs(ValueType other) {
        return this == other || other.isUnknown() || isUnknown();
    }
}
