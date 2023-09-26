package org.hisp.dhis.lib.expression.eval

import org.hisp.dhis.lib.expression.spi.ValueType
import org.hisp.dhis.lib.expression.spi.VariableValue

class ValueTypeVariableValue(private val valueType: ValueType) : VariableValue {

    override fun value(): String? {
        return null
    }

    override fun valueOrDefault(): Any? {
        return null
    }

    override fun candidates(): List<String?>? {
        return null
    }

    override fun eventDate(): String? {
        return null
    }

    override fun valueType(): ValueType {
        return valueType
    }
}
