package org.hisp.dhis.expression;

import org.hisp.dhis.expression.ast.UnaryOperator;
import org.hisp.dhis.expression.spi.IllegalExpressionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the {@link UnaryOperator} evaluation.
 */
class UnaryOperatorExpressionTest {

    @Test
    void testUnaryNot() {
        assertEquals(true, evaluate("!false"));
        assertEquals(false, evaluate("!true"));
        assertEquals(false, evaluate("not (true or false)"));
        assertEquals(true, evaluate("not(true and false)"));
    }

    @Test
    void testUnaryPlus() {
        assertEquals(10.0d, evaluate("++10"));
        assertEquals(8.0d, evaluate("+(+10-2)"));
    }

    @Test
    void testUnaryMinus() {
        assertEquals(-10.0d, evaluate("-10"));
        assertEquals(10.0d, evaluate("--10"));
        assertEquals(-8.0d, evaluate("-(+10-2)"));
    }

    @Test
    void testUnaryDistinct() {
        IllegalExpressionException ex = assertThrows(IllegalExpressionException.class, () -> evaluate("distinct 1"));
        assertEquals("Unary operator not supported for direct evaluation: DISTINCT", ex.getMessage());
    }

    private static Object evaluate(String expression) {
        return new Expression(expression).evaluate();
    }
}
