package org.hisp.dhis.lib.expression.ast

import org.hisp.dhis.lib.expression.spi.ValueType
import java.math.BigDecimal
import java.math.MathContext

/**
 * @author Jan Bernitt
 */
enum class BinaryOperator(@JvmField val symbol: String, private val returnType: ValueType, @JvmField val operandsType: ValueType) : Typed {
    // OBS!!! in order of precedence - highest to lowest
    EXP("^", ValueType.NUMBER, ValueType.NUMBER),

    // unary operators are here precedence wise
    MUL("*", ValueType.NUMBER, ValueType.NUMBER),
    DIV("/", ValueType.NUMBER, ValueType.NUMBER),
    MOD("%", ValueType.NUMBER, ValueType.NUMBER),
    ADD("+", ValueType.NUMBER, ValueType.NUMBER),
    SUB("-", ValueType.NUMBER, ValueType.NUMBER),
    LT("<", ValueType.BOOLEAN, ValueType.NUMBER),
    GT(">", ValueType.BOOLEAN, ValueType.NUMBER),
    LE("<=", ValueType.BOOLEAN, ValueType.NUMBER),
    GE(">=", ValueType.BOOLEAN, ValueType.NUMBER),
    EQ("==", ValueType.BOOLEAN, ValueType.SAME),
    NEQ("!=", ValueType.BOOLEAN, ValueType.SAME),
    AND("&&", ValueType.BOOLEAN, ValueType.BOOLEAN),
    OR("||", ValueType.BOOLEAN, ValueType.BOOLEAN);

    override fun getValueType(): ValueType {
        return returnType
    }

    fun getSymbol() : String {
        return symbol;
    }

    /**
     * @return True for equals and not equals operators, boolean result, two operands of the same type
     */
    fun isEquality(): Boolean {
        return this == EQ || this == NEQ
    }

    /**
     * @return True for any operator that has two number operands and a number result
     */
    fun isArithmetic(): Boolean {
        return !isLogic()
    }

    /**
     * @return True for any operator that can be used on booleans
     */
    fun isLogic(): Boolean {
        return isEquality() || this == AND || this == OR;
    }

    /**
     * @return True for any operator that has a boolean result or a comparison of two operands of the same type
     */
    fun isComparison(): Boolean {
        return isEquality() || isNumericComparison()
    }

    /**
     * @return True for any operator that has a boolean result and two number operands
     */
    fun isNumericComparison(): Boolean {
        return  ordinal >= LT.ordinal
    }

    /**
     * @return True for any operator that can be used on numbers
     */
    fun isNumeric(): Boolean {
        return isArithmetic() || isEquality() || isNumericComparison()
    }


    companion object {
        /**
         * Avoid defensive copy when finding operator by symbol
         */
        private val VALUES = listOf(*entries.toTypedArray())
        @JvmStatic
        fun fromSymbol(symbol: String): BinaryOperator {
            return when (symbol) {
                "and" -> AND
                "or" -> OR
                else -> VALUES.stream().filter { it.symbol == symbol } .findFirst().orElseThrow()
            }
        }

        /*
     Arithmetic Operations
     (all numeric operations either return a Double or an Integer)
     */
        @JvmStatic
        fun add(left: Number, right: Number): Number {
            return if (isSpecialDouble(left) || isSpecialDouble(right)) left.toDouble() + right.toDouble()
            else asBigDecimal(left).add(asBigDecimal(right)).toDouble()
        }

        @JvmStatic
        fun subtract(left: Number, right: Number): Number {
            return if (isSpecialDouble(left) || isSpecialDouble(right)) left.toDouble() - right.toDouble()
            else asBigDecimal(left).subtract(asBigDecimal(right)).toDouble()
        }

        @JvmStatic
        fun multiply(left: Number, right: Number): Number {
            return if (isSpecialDouble(left) || isSpecialDouble(right)) left.toDouble() * right.toDouble()
            else asBigDecimal(left).multiply(asBigDecimal(right)).toDouble()
        }

        @JvmStatic
        fun divide(left: Number, right: Number): Number {
            return if (isSpecialDouble(left) || isSpecialDouble(right) || right.toDouble() == 0.0) left.toDouble() / right.toDouble()
            else asBigDecimal(left).divide(asBigDecimal(right), MathContext.DECIMAL64).toDouble()
        }

        @JvmStatic
        fun modulo(left: Number, right: Number): Number {
            return if (isSpecialDouble(left) || isSpecialDouble(right) || right.toDouble() == 0.0) left.toDouble() % right.toDouble()
            else asBigDecimal(left).remainder(asBigDecimal(right)).toDouble()
        }

        @JvmStatic
        fun exp(base: Number, exponent: Number): Number {
            return if (isSpecialDouble(base) || isSpecialDouble(exponent)) Math.pow(
                base.toDouble(),
                exponent.toDouble()
            )
            else asBigDecimal(base).pow(exponent.toInt(), MathContext.DECIMAL64).toDouble()
        }

        private fun asBigDecimal(n: Number): BigDecimal {
            return n as? BigDecimal ?: BigDecimal(n.toString(), MathContext.DECIMAL64)
        }

        private fun isSpecialDouble(n: Number): Boolean {
            val d = n.toDouble()
            return java.lang.Double.isNaN(d) || java.lang.Double.isInfinite(d)
        }
        /*
    Logic Operations
     */
        /**
         * Any true with null is true, any false/null mix is null.
         *
         * @param left  left-hand side of the operator, maybe null
         * @param right right-hand side of the operator, maybe null
         * @return arguments combined with OR, maybe null
         */
        @JvmStatic
        fun or(left: Boolean?, right: Boolean?): Boolean? {
            if (left == null) {
                return if (right === java.lang.Boolean.TRUE) true else null
            }
            return if (right == null) {
                if (left === java.lang.Boolean.TRUE) true else null
            }
            else left || right
        }

        @JvmStatic
        fun and(left: Boolean?, right: Boolean?): Boolean? {
            return if (left == null || right == null) null else left && right
        }

        /*
    Comparison Operations
     */
        @JvmStatic
        fun lessThan(left: Any, right: Any?): Boolean {
            return if (left is String && right is String) left < right
            else Typed.toNumberTypeCoercion(left) < Typed.toNumberTypeCoercion(right)
        }

        @JvmStatic
        fun lessThanOrEqual(left: Any, right: Any?): Boolean {
            return if (left is String && right is String) left <= right
            else Typed.toNumberTypeCoercion(left) <= Typed.toNumberTypeCoercion(right)
        }

        @JvmStatic
        fun greaterThan(left: Any, right: Any?): Boolean {
            return if (left is String && right is String) left > right
            else Typed.toNumberTypeCoercion(left) > Typed.toNumberTypeCoercion(right)
        }

        @JvmStatic
        fun greaterThanOrEqual(left: Any, right: Any?): Boolean {
            return if (left is String && right is String) left >= right
            else Typed.toNumberTypeCoercion(left) >= Typed.toNumberTypeCoercion(right)
        }

        /*
    Equality Operations
     */
        @JvmStatic
        fun equal(left: Any?, right: Any?): Boolean {
            if (left == null || right == null) {
                return left == null && right == null
            }
            if (left is Boolean || right is Boolean) {
                return Typed.toBooleanTypeCoercion(left) == Typed.toBooleanTypeCoercion(right)
            }
            return if (left is Number || right is Number) {
                Typed.toNumberTypeCoercion(left) == Typed.toNumberTypeCoercion(
                    right
                )
            } else left == right
        }

        @JvmStatic
        fun notEqual(left: Any?, right: Any?): Boolean {
            return !equal(left, right)
        }
    }
}
