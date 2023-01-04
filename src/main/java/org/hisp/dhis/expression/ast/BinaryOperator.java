package org.hisp.dhis.expression.ast;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

import static java.lang.Double.isInfinite;
import static java.lang.Double.isNaN;
import static java.lang.String.format;
import static org.hisp.dhis.expression.ast.ValueType.BOOLEAN;
import static org.hisp.dhis.expression.ast.ValueType.NUMBER;
import static org.hisp.dhis.expression.ast.ValueType.SAME;

/**
 *
 * @author Jan Bernitt
 */
public enum BinaryOperator implements Typed
{
    // OBS!!! in order of precedence - highest to lowest
    EXP("^", NUMBER, NUMBER),
    MUL("*", NUMBER, NUMBER),
    DIV("/", NUMBER, NUMBER),
    MOD("%", NUMBER, NUMBER),
    ADD("+", NUMBER, NUMBER),
    SUB("-", NUMBER, NUMBER),
    AND("&&", BOOLEAN, BOOLEAN),
    OR("||", BOOLEAN, BOOLEAN),
    EQ("==", BOOLEAN, SAME),
    NEQ("!=", BOOLEAN, SAME),
    LT("<", BOOLEAN, NUMBER),
    GT(">", BOOLEAN, NUMBER),
    LE("<=", BOOLEAN, NUMBER),
    GE(">=", BOOLEAN, NUMBER);

    private final String symbol;
    private final ValueType returnType;
    private final ValueType operandsType;

    BinaryOperator(String symbol, ValueType returnType, ValueType operandsType) {
        this.symbol = symbol;
        this.returnType = returnType;
        this.operandsType = operandsType;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public ValueType getValueType() {
        return returnType;
    }

    public ValueType getOperandsType() {
        return operandsType;
    }

    /**
     * Avoid defensive copy when finding operator by symbol
     */
    private static final List<BinaryOperator> VALUES = List.of(values());

    static BinaryOperator fromSymbol(String symbol) {
        return VALUES.stream().filter(op -> op.symbol.equals(symbol)).findFirst().orElseThrow();
    }

    /**
     * @return True for equals and not equals operators, boolean result, two operands of the same type
     */
    public boolean isEquality() {
        return this == EQ || this == NEQ;
    }

    /**
     * @return True for any operator that has two number operands and a number result
     */
    public boolean isArithmetic() {
        return !isLogic();
    }

    /**
     * @return True for any operator that can be used on booleans
     */
    public boolean isLogic() {
        return isEquality() || this == AND || this == OR;
    }

    /**
     * @return True for any operator that has a boolean result or a comparison of two operands of the same type
     */
    public boolean isComparison() {
        return isEquality() || isNumericComparison();
    }

    /**
     * @return True for any operator that has a boolean result and two number operands
     */
    public boolean isNumericComparison() {
        return ordinal() >= LT.ordinal();
    }

    /**
     * @return True for any operator that can be used on numbers
     */
    public boolean isNumeric() {
        return isArithmetic() || isEquality() || isNumericComparison();
    }


    /*
     Arithmetic Operations
     (all numeric operations either return a Double or an Integer)
     */

    public static Number add(Number left, Number right) {
        return isSpecialDouble(left) || isSpecialDouble(right)
                ? left.doubleValue() + right.doubleValue()
                : asBigDecimal(left).add(asBigDecimal(right)).doubleValue();
    }

    public static Number subtract(Number left, Number right) {
        return isSpecialDouble(left) || isSpecialDouble(right)
                ? left.doubleValue() - right.doubleValue()
                : asBigDecimal(left).subtract(asBigDecimal(right)).doubleValue();
    }

    public static Number multiply(Number left, Number right) {
        return isSpecialDouble(left) || isSpecialDouble(right)
                ? left.doubleValue() * right.doubleValue()
                : asBigDecimal(left).multiply(asBigDecimal(right)).doubleValue();
    }

    public static Number divide(Number left, Number right)
    {
        return isSpecialDouble(left) || isSpecialDouble(right) || right.doubleValue() == 0d
                ? left.doubleValue() / right.doubleValue()
                : asBigDecimal(left).divide(asBigDecimal(right), MathContext.DECIMAL64).doubleValue();
    }

    public static Number modulo(Number left, Number right) {
        return isSpecialDouble(left) || isSpecialDouble(right) || right.doubleValue() == 0d
                        ? left.doubleValue() % right.doubleValue()
                        : asBigDecimal(left).remainder(asBigDecimal(right)).doubleValue();
    }

    public static Number exp(Number base, Number exponent) {
        return isSpecialDouble(base) || isSpecialDouble(exponent)
                ? Math.pow(base.doubleValue(), exponent.doubleValue())
                : asBigDecimal(base).pow(exponent.intValue(), MathContext.DECIMAL64).doubleValue();
    }

    private static BigDecimal asBigDecimal(Number n) {
        return n instanceof BigDecimal ? (BigDecimal) n : new BigDecimal(n.toString(), MathContext.DECIMAL64);
    }

    private static boolean isSpecialDouble(Number n) {
        double d = n.doubleValue();
        return isNaN(d) || isInfinite(d);
    }

    /*
    Logic Operations
     */

    /**
     * Any true with null is true, any false/null mix is null.
     *
     * @param left left-hand side of the operator, maybe null
     * @param right right-hand side of the operator, maybe null
     * @return arguments combined with OR, maybe null
     */
    public static Boolean or(Object left, Object right) {
        Boolean l = toBooleanTypeCoercion(left);
        Boolean r = toBooleanTypeCoercion(right);
        if (l == null) {
            return r == Boolean.TRUE ? true : null;
        }
        if (r == null)
        {
            return l == Boolean.TRUE ? true : null;
        }
        return l || r;
    }

    public static Boolean and(Object left, Object right) {
        Boolean l = toBooleanTypeCoercion(left);
        Boolean r = toBooleanTypeCoercion(right);
        return l == null || r == null ? null :  l && r;
    }

    /*
    Comparison Operations
     */

    public static boolean lessThan(Object left, Object right)
    {
        return left instanceof String && right instanceof String
                ? ((String) left).compareTo((String) right) < 0
                : toNumberTypeCoercion(left) < toNumberTypeCoercion(right);
    }

    public static boolean lessThanOrEqual(Object left, Object right)
    {
        return left instanceof String && right instanceof String
                ? ((String) left).compareTo((String) right) <= 0
                : toNumberTypeCoercion(left) <= toNumberTypeCoercion(right);
    }

    public static boolean greaterThan(Object left, Object right)
    {
        return left instanceof String && right instanceof String
                ? ((String) left).compareTo((String) right) > 0
                : toNumberTypeCoercion(left) > toNumberTypeCoercion(right);
    }

    public static boolean greaterThanOrEqual(Object left, Object right)
    {
        return left instanceof String && right instanceof String
                ? ((String) left).compareTo((String) right) >= 0
                : toNumberTypeCoercion(left) >= toNumberTypeCoercion(right);
    }

    /*
    Equality Operations
     */

    public static boolean equal(Object left, Object right) {
        if (left == null || right == null)
        {
            return left == null && right == null;
        }
        if (left instanceof Boolean || right instanceof Boolean)
        {
            return toBooleanTypeCoercion(left).equals(toBooleanTypeCoercion(right));
        }
        if (left instanceof Number || right instanceof Number) {
            return toNumberTypeCoercion(left).equals(toNumberTypeCoercion(right));
        }
        return left.equals(right);
    }

    public static boolean notEqual(Object left, Object right) {
        return !equal(left, right);
    }

    static Double toNumberTypeCoercion(Object value)
    {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return value == Boolean.TRUE ? 1d : 0d;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.valueOf(value.toString());
    }

    static Boolean toBooleanTypeCoercion(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            if (!isInteger((Number) value)) {
                throw new IllegalArgumentException(format("Could not cast Double '%s' to Boolean", value));
            }
            return ((Number) value).intValue() != 0;
        }
        return Boolean.valueOf(value.toString());
    }

    static boolean isInteger(Number value) {
        return value.doubleValue() % 1.0d == 0d;
    }
}
