package org.hisp.dhis.lib.expression.ast

import org.hisp.dhis.lib.expression.spi.ValueType

/**
 * A.K.A "dot functions"
 *
 * @author Jan Bernitt
 */
enum class DataItemModifier(vararg parameterTypes: ValueType) : Typed {

    aggregationType(ValueType.STRING),
    maxDate(ValueType.DATE),
    minDate(ValueType.DATE),
    periodOffset(ValueType.NUMBER),
    stageOffset(ValueType.NUMBER),
    yearToDate,
    periodAggregation,
    subExpression;

    @JvmField
    val parameterTypes: List<ValueType>

    init {
        this.parameterTypes = listOf(*parameterTypes)
    }

    override fun getValueType(): ValueType {
        return ValueType.SAME
    }
}
