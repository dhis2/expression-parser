package org.hisp.dhis.lib.expression.ast

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.atTime
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.alternativeParsing
import kotlinx.datetime.format.char
import kotlinx.datetime.format.optional
import org.hisp.dhis.lib.expression.spi.ValueType
import org.hisp.dhis.lib.expression.spi.VariableValue
import kotlin.time.Instant

fun interface Typed {

    fun getValueType(): ValueType

    companion object {
        fun toNumberTypeCoercion(value: Any?): Double? {
            if (value == null) return null
            if (value is VariableValue) return toNumberTypeCoercion(toMixedTypeTypeCoercion(value))
            if (value is Boolean) return if (value == true) 1.0 else 0.0
            if (value is LocalDate) return value.toEpochDays().toDouble()
            if (value is Instant) return value.toEpochMilliseconds().toDouble()
            return (value as? Number)?.toDouble() ?: value.toString().toDouble()
        }

        fun toBooleanTypeCoercion(value: Any?): Boolean? {
            if (value == null) return null
            if (value is VariableValue) return toBooleanTypeCoercion(toMixedTypeTypeCoercion(value))
            if (value is Boolean) return value
            if (value is Number) {
                require(isNonFractionValue(value)) { "Could not coerce Double '$value' to Boolean" }
                return value.toInt() != 0
            }
            return value.toString().toBoolean()
        }

        fun toDateTypeCoercion(value: Any?): LocalDate? {
            if (value == null) return null
            if (value is VariableValue) return toDateTypeCoercion(toMixedTypeTypeCoercion(value))
            if (value is LocalDate) return value
            if (value is String) return dateTimeFormat.parse(value).toLocalDate()
            if (value is Instant) return value.toLocalDateTime(TimeZone.currentSystemDefault()).date
            throw IllegalArgumentException("Count not coerce to date: '$value'")
        }

        fun toInstantTypeCoercion(value: Any?): Instant? {
            if (value == null) return null
            if (value is VariableValue)
                return if (value.valueType == ValueType.DATE) toInstantTypeCoercion(value.valueOrDefault())
                else toInstantTypeCoercion(toMixedTypeTypeCoercion(value))
            if (value is LocalDate) return value.atTime(0, 0).toInstant(TimeZone.UTC)
            if (value is String) return dateTimeFormat.parse(value).apply {
                    hour = hour ?: 0
                    minute = minute ?: 0
                    second = second ?: 0
                    nanosecond = nanosecond ?: 0
                    if (offsetHours == null) {
                        setDateTimeOffset(toLocalDateTime(), UtcOffset.ZERO)
                    }
                }.toInstantUsingOffset()
            if (value is Instant) return value
            throw IllegalArgumentException("Count not coerce to instant: '$value'")
        }

        fun toStringTypeCoercion(value: Any?): String? {
            if (value == null) return null
            if (value is VariableValue) return toStringTypeCoercion(toMixedTypeTypeCoercion(value));
            if (value is Number) return  if (isNonFractionValue(value)) value.toInt().toString() else value.toString();
            return value.toString()
        }

        fun toMixedTypeTypeCoercion(value: Any?): Any? {
            if (value == null) return null
            return if (value is VariableValue) {
                when (value.valueType) {
                    ValueType.NUMBER -> toNumberTypeCoercion(value.valueOrDefault())
                    ValueType.BOOLEAN -> toBooleanTypeCoercion(value.valueOrDefault())
                    ValueType.DATE -> toDateTypeCoercion(value.valueOrDefault())
                    else -> toStringTypeCoercion(value.valueOrDefault())
                }
            }
            else value
        }

        private fun isNonFractionValue(value: Number): Boolean {
            return value.toDouble() % 1.0 == 0.0
        }

        private val dateTimeFormat = DateTimeComponents.Format {
            date(LocalDate.Formats.ISO)
            alternativeParsing({}) {
                alternativeParsing({ char(' ') }) { char('T') }
                hour()
                alternativeParsing({}) {
                    char(':')
                    minute()
                    optional {
                        char(':')
                        second()
                        optional {
                            char('.')
                            secondFraction()
                        }
                    }
                }
            }
            optional { offset(UtcOffset.Formats.ISO) }
        }
    }
}
