package org.hisp.dhis.expression.ast;

import java.util.List;

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
    stageOffset(ValueType.NUMBER),
    yearToDate();

    private final List<ValueType> parameterTypes;

    DataItemModifier(ValueType... parameterTypes) {
        this.parameterTypes = List.of(parameterTypes);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.SAME;
    }

    public List<ValueType> getParameterTypes() {
        return parameterTypes;
    }
}
