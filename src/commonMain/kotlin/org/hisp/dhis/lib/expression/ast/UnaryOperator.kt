package org.hisp.dhis.lib.expression.ast

import org.hisp.dhis.lib.expression.spi.ValueType

/**
 * @param symbol the canonical symbol of the operator
 * @param aliases alternative spellings of the operator that are also accepted by the parser
 */
enum class UnaryOperator(val symbol: String, private val returnType: ValueType, vararg aliases: String) : Typed {

    PLUS("+", ValueType.NUMBER),
    MINUS("-", ValueType.NUMBER),
    NOT("!", ValueType.BOOLEAN, "not"),
    DISTINCT("distinct", ValueType.SAME);

    val aliases: List<String> = listOf(*aliases)

    override fun getValueType(): ValueType {
        return returnType
    }

    companion object {

        fun fromSymbol(symbol: String): UnaryOperator {
            return entries.first { op: UnaryOperator -> op.symbol == symbol || symbol in op.aliases }
        }

        fun negate(value: Number?): Number? {
            if (value == null) {
                return null
            }
            return -value.toDouble()
        }
    }
}
