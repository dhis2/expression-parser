package org.hisp.dhis.expression;

import java.util.List;

/**
 *
 * @author Jan Bernitt
 */
public enum BinaryOperator
{
    // OBS!!! in order of precedence - highest to lowest
    EXP("^"),
    MUL("*"),
    DIV("/"),
    MOD("%"),
    ADD("+"),
    SUB("-"),
    AND("&&"),
    OR("||"),
    EQ("=="),
    NEQ("!="),
    LT("<"),
    GT(">"),
    LE("<="),
    GE(">=");

    private final String symbol;

    BinaryOperator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
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
}
