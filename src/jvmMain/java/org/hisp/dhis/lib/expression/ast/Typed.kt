package org.hisp.dhis.lib.expression.ast

import org.hisp.dhis.lib.expression.spi.ValueType
import org.hisp.dhis.lib.expression.spi.VariableValue
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

interface Typed {

    fun getValueType(): ValueType

    companion object {
        fun toNumberTypeCoercion(value: Any?): Double? {
            if (value == null) return null
            if (value is VariableValue) return toNumberTypeCoercion(value.valueOrDefault())
            return if (value is Boolean) if (value == true) 1.0 else 0.0
            else (value as? Number)?.toDouble() ?: java.lang.Double.valueOf(value.toString())
        }

        fun toBooleanTypeCoercion(value: Any?): Boolean? {
            if (value == null) return null
            if (value is VariableValue) return toBooleanTypeCoercion(value.valueOrDefault())
            if (value is Boolean) return value
            if (value is Number) {
                require(isNonFractionValue(value)) {
                    String.format(
                        "Could not coerce Double '%s' to Boolean",
                        value
                    )
                }
                return value.toInt() != 0
            }
            return java.lang.Boolean.valueOf(value.toString())
        }

        fun toDateTypeCoercion(value: Any?): LocalDate? {
            if (value == null) return null
            if (value is VariableValue) return toDateTypeCoercion(value.valueOrDefault())
            if (value is LocalDate) return value
            if (value is String) return LocalDate.parse(value)
            if (value is Date) return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            throw IllegalArgumentException(String.format("Count not coerce to date: '%s'", value))
        }

        fun toStringTypeCoercion(value: Any?): String? {
            return if (value == null) null
            else (value as? VariableValue)?.valueOrDefault()?.toString() ?: value.toString()
        }

        fun toMixedTypeTypeCoercion(value: Any?): Any? {
            if (value == null) return null
            return if (value is VariableValue) {
                when (value.valueType()) {
                    ValueType.NUMBER -> toNumberTypeCoercion(
                        value.valueOrDefault()
                    )

                    ValueType.BOOLEAN -> toBooleanTypeCoercion(
                        value.valueOrDefault()
                    )

                    ValueType.DATE -> toDateTypeCoercion(
                        value.valueOrDefault()
                    )

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
