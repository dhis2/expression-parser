package org.hisp.dhis.expression;

/**
 * A.K.A "dot functions"
 *
 * @author Jan Bernitt
 */
@SuppressWarnings("java:S115")
public enum NamedMethod
{
    aggregationType(ValueType.UNKNOWN, ValueType.STRING),
    maxDate(ValueType.DATE, ValueType.DATE),
    minDate(ValueType.DATE, ValueType.DATE),
    periodOffset(ValueType.UNKNOWN, ValueType.NUMBER),
    stageOffset(ValueType.UNKNOWN, ValueType.NUMBER);

    private final ValueType returnType;
    private final ValueType argumentType;

    NamedMethod(ValueType returnType, ValueType argumentType) {

        this.returnType = returnType;
        this.argumentType = argumentType;
    }

    public ValueType returnType() {
        return returnType;
    }

    public ValueType argumentType() {
        return argumentType;
    }
}
