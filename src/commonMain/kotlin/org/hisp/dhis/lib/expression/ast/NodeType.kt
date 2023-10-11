package org.hisp.dhis.lib.expression.ast

enum class NodeType {
    // complex nodes
    PAR,
    ARGUMENT,
    FUNCTION,
    MODIFIER,

    /**
     * <pre>
     * DATA_ITEM => (ARGUMENT => (IDENTIFIER? UID+)){1,3}
    </pre> *
     */
    DATA_ITEM,

    /**
     * 1. A program rule variable name or program variable name
     * <pre>
     * VARIABLE => IDENTIFIER
    </pre> *
     *
     *
     * 2. A program rule variable name string
     * <pre>
     * VARIABLE => STRING
    </pre> *
     */
    VARIABLE,

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
    BOOLEAN;

    fun isOperator(): Boolean {
        return this == UNARY_OPERATOR || this == BINARY_OPERATOR
    }

    fun isComplex(): Boolean {
        return !isSimple();
    }
    fun isSimple(): Boolean {
        return ordinal > UNARY_OPERATOR.ordinal
    }

    fun isConstant(): Boolean {
        return ordinal >= NULL.ordinal
    }

    /**
     * Special purpose literals like [.UID] or [.IDENTIFIER] are not considered literals as they should not
     * be used in places where literals for the [org.hisp.dhis.lib.expression.spi.ValueType]s can occur.
     *
     * @return true if this is a general literal for no special purpose
     */
    fun isValueLiteral(): Boolean {
        return isConstant() || this == NUMBER || this == INTEGER || this == STRING || this == DATE
    }
}
