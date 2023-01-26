package org.hisp.dhis.expression.ast;

public enum VariableType {

    PROGRAM,
    PROGRAM_RULE;

    public static VariableType fromSymbol(String symbol) {
        return "V".equals(symbol) ? PROGRAM : PROGRAM_RULE;
    }
}
