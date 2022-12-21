package org.hisp.dhis.expression.ast;

/**
 * A.K.A "dot functions"
 *
 * @author Jan Bernitt
 */
@SuppressWarnings("java:S115")
public enum DataItemModifier implements Typed
{
    aggregationType(ValueType.STRING),
    maxDate(ValueType.DATE),
    minDate( ValueType.DATE),
    periodOffset(ValueType.NUMBER),
    stageOffset(ValueType.NUMBER);

    private final ValueType parameterType;

    DataItemModifier(ValueType parameterType) {
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
