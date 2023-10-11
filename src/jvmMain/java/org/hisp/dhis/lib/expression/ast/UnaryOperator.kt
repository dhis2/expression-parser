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

        fun fromSymbol(symbol: String): UnaryOperator {
            return if ("not" == symbol) NOT else entries.first { op: UnaryOperator -> op.symbol == symbol }
        }

        fun negate(value: Number?): Number? {
            if (value == null) {
                return null
            }
            return if (value is Int) -value.toInt() else -value.toDouble()
        }
    }
}
