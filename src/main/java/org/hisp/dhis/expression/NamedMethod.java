package org.hisp.dhis.expression;

/**
 * A.K.A "dot functions"
 *
 * @author Jan Bernitt
 */
@SuppressWarnings("java:S115")
public enum NamedMethod implements Typed
{
    aggregationType(ValueType.STRING),
    maxDate(ValueType.DATE),
    minDate( ValueType.DATE),
    periodOffset(ValueType.NUMBER),
    stageOffset(ValueType.NUMBER);

    private final ValueType parameterType;

    NamedMethod(ValueType parameterType) {
        this.parameterType = parameterType;
    }

    @Override
    public ValueType getValueType() {
        return ValueType.SAME;
    }

    public ValueType getParameterType() {
        return parameterType;
    }
}
