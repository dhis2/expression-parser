package org.hisp.dhis.lib.expression.spi

import java.time.LocalDate
import java.util.Date

/**
 * A rough classification of what values building blocks expect and return.
 *
 *
 * These can be understood as semantic value types.
 *
 * @author Jan Bernitt
 */
enum class ValueType {
    /**
     * Type can be at least two of the following: numbers, booleans, dates, strings, list/array of these. This is also
     * used in case a type is unknown or cannot be determined statically.
     */
    MIXED,
    NUMBER,
    BOOLEAN,
    DATE,
    STRING,

    /**
     * Means the type can be mixed but all SAME argument should be of the same actual type. If the return type is also
     * SAME it is the actual type of the SAME parameter type.
     */
    SAME;

    fun isSame(): Boolean {
        return this == SAME
    }

    fun isMixed(): Boolean {
        return this == MIXED
    }


    /**
     * **Can** a value of this type **definitely** be used for the other type.
     *
     *
     * This means the two are type compatible. If [.MIXED] is involved this still might fail with certain actual
     * values.
     *
     * @param other target type
     * @return true, if this type (actual) is assignable to the other (required), otherwise false
     */
    fun isAssignableTo(other: ValueType): Boolean {
        return !isSame() && (this == other || isMixed() || other.isMixed() || other.isSame() || this == STRING && other == DATE) // there are no date literals, so strings are used
    }

    /**
     * **May** a value of this **potentially** be usable for the other type.
     *
     *
     * This means a value potentially can be converted to the required type using a known conversion. Such conversion
     * might fail because the input did not have the required format.
     *
     * @param other target type
     * @return true, if this type (actual) is assignable or convertable to the other (required), otherwise false
     */
    fun isMaybeAssignableTo(other: ValueType): Boolean {
        return if (isAssignableTo(other)) true else when (other) {
            STRING -> true
            NUMBER -> this == STRING || this == BOOLEAN
            DATE -> this == STRING
            BOOLEAN -> this == STRING || this == NUMBER
            else -> false
        }
    }

    fun isValidValue(value: Any?): Boolean {
        return when (this) {
            STRING -> value is String
            NUMBER -> value is Number
            BOOLEAN -> value is Boolean
            DATE -> value is LocalDate || value is Date
            else -> true
        }
    }

    companion object {
        fun allSame(types: List<ValueType>): Boolean {
            return types.isEmpty() || types.filter { it != MIXED } .all {it == types[0]}
        }
    }
}
