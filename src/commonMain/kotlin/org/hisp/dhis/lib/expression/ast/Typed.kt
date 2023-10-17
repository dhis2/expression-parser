package org.hisp.dhis.lib.expression.ast

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.hisp.dhis.lib.expression.spi.ValueType
import org.hisp.dhis.lib.expression.spi.VariableValue

fun interface Typed {

    fun getValueType(): ValueType

    companion object {
        fun toNumberTypeCoercion(value: Any?): Double? {
            if (value == null) return null
            if (value is VariableValue) return toNumberTypeCoercion(value.valueOrDefault())
            return if (value is Boolean) if (value == true) 1.0 else 0.0
            else (value as? Number)?.toDouble() ?: value.toString().toDouble()
        }

        fun toBooleanTypeCoercion(value: Any?): Boolean? {
            if (value == null) return null
            if (value is VariableValue) return toBooleanTypeCoercion(value.valueOrDefault())
            if (value is Boolean) return value
            if (value is Number) {
                require(isNonFractionValue(value)) { "Could not coerce Double '$value' to Boolean" }
                return value.toInt() != 0
            }
            return value.toString().toBoolean()
        }

        fun toDateTypeCoercion(value: Any?): LocalDate? {
            if (value == null) return null
            if (value is VariableValue) return toDateTypeCoercion(value.valueOrDefault())
            if (value is LocalDate) return value
            if (value is String) return LocalDate.parse(value)
            if (value is Instant) return value.toLocalDateTime(TimeZone.currentSystemDefault()).date
            throw IllegalArgumentException("Count not coerce to date: '$value'")
        }

        fun toStringTypeCoercion(value: Any?): String? {
            if (value == null) return null
            if (value is VariableValue) return toStringTypeCoercion(value.valueOrDefault());
            if (value is Number) return  if (isNonFractionValue(value)) value.toInt().toString() else value.toString();
            return value.toString()
        }

        fun toMixedTypeTypeCoercion(value: Any?): Any? {
            if (value == null) return null
            return if (value is VariableValue) {
                when (value.valueType()) {
                    ValueType.NUMBER -> toNumberTypeCoercion(value.valueOrDefault())
                    ValueType.BOOLEAN -> toBooleanTypeCoercion(value.valueOrDefault())
                    ValueType.DATE -> toDateTypeCoercion(value.valueOrDefault())
                    else -> toStringTypeCoercion(value.valueOrDefault())
                }
            }
            else value
        }

        fun isNonFractionValue(value: Number): Boolean {
            return value.toDouble() % 1.0 == 0.0
        }
    }
}
