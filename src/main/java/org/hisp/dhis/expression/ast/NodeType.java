package org.hisp.dhis.expression.ast;

public enum NodeType
{
    // complex nodes
    PAR,
    ARGUMENT,

    FUNCTION,
    MODIFIER,
    /**
     * A DATA_ITEM takes one of 3 forms in its subtree:
     *
     * 1. Classic data item with 1-3 UID positions (each might have a tag and 1 or more UIDs)
     * <pre>
     * DATA_ITEM => (ARGUMENT => (IDENTIFIER? UID+)){1,3}
     * </pre>
     *
     * 2. A program rule variable name
     * <pre>
     * DATA_ITEM => IDENTIFIER
     * </pre>
     *
     * 3. A program rule variable name string
     * <pre>
     * DATA_ITEM => STRING
     * </pre>
     *
     */
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
