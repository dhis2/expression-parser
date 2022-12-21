package org.hisp.dhis.expression.ast;

public enum NodeType
{
    // complex nodes
    PAR,
    ARGUMENT,

    FUNCTION,
    MODIFIER,
    DATA_ITEM,

    // operators
    BINARY_OPERATOR,
    UNARY_OPERATOR,

    // simple nodes (literals)
    NUMBER,
    INTEGER,
    STRING,
    DATE,
    UID,
    IDENTIFIER,
    NAMED_VALUE,

    // constants
    NULL,
    BOOLEAN,
    ;

    public boolean isOperator() {
        return this == UNARY_OPERATOR || this == BINARY_OPERATOR;
    }

    public boolean isComplex() {
        return !isSimple();
    }

    public boolean isSimple() {
        return ordinal() > UNARY_OPERATOR.ordinal();
    }

    public boolean isConstant() {
        return ordinal() >= NULL.ordinal();
    }

}
