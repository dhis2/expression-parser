package org.hisp.dhis.lib.expression.util

import org.hisp.dhis.lib.expression.spi.ValueType
import org.hisp.dhis.lib.expression.spi.VariableValue

data class RuleVariableValue (
    private val valueType: ValueType,
    private val value: String?,
    private val candidates: List<String>?,
    private val eventDate: String?
) : VariableValue {

    constructor(valueType: ValueType) : this(valueType, null, null, null)

    override fun value(): String? {
        return value
    }

    override fun valueOrDefault(): Any {
        return when (valueType) {
            ValueType.NUMBER -> 0.0
            ValueType.DATE -> "2010-01-01"
            ValueType.BOOLEAN -> false
            else -> ""
        }
    }

    override fun candidates(): List<String>? {
        return candidates
    }

    override fun eventDate(): String? {
        return eventDate
    }

    override fun valueType(): ValueType {
        return valueType
    }
}
