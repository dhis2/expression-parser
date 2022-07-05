package org.hisp.dhis.expression;

import java.util.List;

import static java.lang.Math.negateExact;

public enum UnaryOperator implements Typed{

    PLUS("+", ValueType.NUMBER),
    MINUS("-", ValueType.NUMBER),
    NOT("!", ValueType.BOOLEAN),
    DISTINCT("distinct", ValueType.SAME);

    private final String symbol;
    private final ValueType returnType;

    UnaryOperator(String symbol, ValueType returnType) {
        this.symbol = symbol;
        this.returnType = returnType;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public ValueType getValueType() {
        return returnType;
    }

    /**
     * Avoid defensive copy when finding operator by symbol
     */
    private static final List<UnaryOperator> VALUES = List.of(values());

    static UnaryOperator fromSymbol(String symbol) {
        return VALUES.stream().filter(op -> op.symbol.equals(symbol)).findFirst().orElseThrow();
    }

    public static Number negate(Number value) {
        if (value == null)
        {
            return null;
        }
        return value instanceof Integer ? negateExact(value.intValue()) : -value.doubleValue();
    }
}
