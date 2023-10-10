package org.hisp.dhis.lib.expression.ast

import org.hisp.dhis.lib.expression.spi.ValueType

enum class UnaryOperator(val symbol: String, private val returnType: ValueType) : Typed {

    PLUS("+", ValueType.NUMBER),
    MINUS("-", ValueType.NUMBER),
    NOT("!", ValueType.BOOLEAN),
    DISTINCT("distinct", ValueType.SAME);

    override fun getValueType(): ValueType {
        return returnType
    }

    companion object {
        /**
         * Avoid defensive copy when finding operator by symbol
         */
        private val VALUES = listOf(*entries.toTypedArray())

        fun fromSymbol(symbol: String): UnaryOperator {
            return if ("not" == symbol) NOT else VALUES.stream().filter { op: UnaryOperator -> op.symbol == symbol }
                .findFirst().orElseThrow()
        }

        fun negate(value: Number?): Number? {
            if (value == null) {
                return null
            }
            return if (value is Int) Math.negateExact(value.toInt()) else -value.toDouble()
        }
    }
}
