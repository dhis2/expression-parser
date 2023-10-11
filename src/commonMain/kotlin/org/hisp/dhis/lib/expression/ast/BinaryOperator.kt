package org.hisp.dhis.lib.expression.ast

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import org.hisp.dhis.lib.expression.spi.ValueType
import kotlin.math.pow

/**
 * @author Jan Bernitt
 */
enum class BinaryOperator(val symbol: String, private val returnType: ValueType, val operandsType: ValueType) : Typed {
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

        fun fromSymbol(symbol: String): BinaryOperator {
            return when (symbol) {
                "and" -> AND
                "or" -> OR
                else -> entries.first { it.symbol == symbol }
            }
        }

        /*
        Arithmetic Operations
        (all numeric operations either return a Double or an Integer)
        */

        private val MODE: DecimalMode = DecimalMode(16, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)

        fun add(left: Number?, right: Number?): Number {
            require(left != null) { "left operator of addition must not be null" }
            require(right != null) { "right operator of addition must not be null" }
            return if (isSpecialDouble(left) || isSpecialDouble(right)) left.toDouble() + right.toDouble()
            else asBigDecimal(left).add(asBigDecimal(right)).doubleValue(false)
        }

        fun subtract(left: Number?, right: Number?): Number {
            require(left != null) { "left operator of subtraction must not be null" }
            require(right != null) { "right operator of subtraction must not be null" }
            return if (isSpecialDouble(left) || isSpecialDouble(right)) left.toDouble() - right.toDouble()
            else asBigDecimal(left).subtract(asBigDecimal(right)).doubleValue(false)
        }

        fun multiply(left: Number?, right: Number?): Number {
            require(left != null) { "left operator of multiplication must not be null" }
            require(right != null) { "right operator of multiplication must not be null" }
            return if (isSpecialDouble(left) || isSpecialDouble(right)) left.toDouble() * right.toDouble()
            else asBigDecimal(left).multiply(asBigDecimal(right)).doubleValue(false)
        }

        fun divide(left: Number?, right: Number?): Number {
            require(left != null) { "left operator of division must not be null" }
            require(right != null) { "right operator of division must not be null" }
            return if (isSpecialDouble(left) || isSpecialDouble(right) || right.toDouble() == 0.0) left.toDouble() / right.toDouble()
            else asBigDecimal(left).divide(asBigDecimal(right)).doubleValue(false)
        }

        fun modulo(left: Number?, right: Number?): Number {
            require(left != null) { "left operator of modulo must not be null" }
            require(right != null) { "right operator of modulo must not be null" }
            return if (isSpecialDouble(left) || isSpecialDouble(right) || right.toDouble() == 0.0) left.toDouble() % right.toDouble()
            else BigDecimal.fromDouble(left.toDouble()).remainder(BigDecimal.fromDouble(right.toDouble())).doubleValue(false)
        }

        fun exp(base: Number?, exponent: Number?): Number {
            require(base != null) { "base operator of exponential function must not be null" }
            require(exponent != null) { "exponent operator of exponential function must not be null" }
            return if (isSpecialDouble(base) || isSpecialDouble(exponent)) base.toDouble().pow(exponent.toDouble())
            else asBigDecimal(base).pow(exponent.toInt()).doubleValue(false)
        }

        private fun asBigDecimal(n: Number): BigDecimal {
            return BigDecimal.fromDouble(n.toDouble(), MODE)
        }

        private fun isSpecialDouble(n: Number?): Boolean {
            if (n == null) return false
            val d = n.toDouble()
            return d.isNaN() || d.isInfinite()
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
        fun or(left: Boolean?, right: Boolean?): Boolean? {
            if (left == null)
                return if (right == true) true else null
            if (right == null)
                return if (left == true) true else null
            return left || right
        }

        fun and(left: Boolean?, right: Boolean?): Boolean? {
            return if (left == null || right == null) null else left && right
        }

        /*
        Comparison Operations
        */

        fun lessThan(left: Any?, right: Any?): Boolean {
            return if (left is String && right is String) left < right
            else Typed.toNumberTypeCoercion(left)!! < Typed.toNumberTypeCoercion(right)!!
        }


        fun lessThanOrEqual(left: Any?, right: Any?): Boolean {
            return if (left is String && right is String) left <= right
            else Typed.toNumberTypeCoercion(left)!! <= Typed.toNumberTypeCoercion(right)!!
        }


        fun greaterThan(left: Any?, right: Any?): Boolean {
            return if (left is String && right is String) left > right
            else Typed.toNumberTypeCoercion(left)!! > Typed.toNumberTypeCoercion(right)!!
        }


        fun greaterThanOrEqual(left: Any?, right: Any?): Boolean {
            return if (left is String && right is String) left >= right
            else Typed.toNumberTypeCoercion(left)!! >= Typed.toNumberTypeCoercion(right)!!
        }

        /*
        Equality Operations
        */

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


        fun notEqual(left: Any?, right: Any?): Boolean {
            return !equal(left, right)
        }
    }
}
