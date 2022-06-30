package org.hisp.dhis.expression;

import java.util.List;

public enum UnaryOperator {

    PLUS("+"),
    MINUS("-"),
    NOT("!"),
    DISTINCT("distinct");

    private final String symbol;

    UnaryOperator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    /**
     * Avoid defensive copy when finding operator by symbol
     */
    private static final List<UnaryOperator> VALUES = List.of(values());

    static UnaryOperator fromSymbol(String symbol) {
        return VALUES.stream().filter(op -> op.symbol.equals(symbol)).findFirst().orElseThrow();
    }
}
