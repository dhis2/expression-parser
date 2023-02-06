package org.hisp.dhis.lib.expression;

import org.hisp.dhis.lib.expression.spi.IllegalExpressionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test of the {@code d2:ceil} function.
 *
 * Translated from existing test of same name in rule-engine.
 *
 * @author Jan Bernitt
 */
class RuleFunctionCeilTest {

    @Test
    void evaluateMustReturnCeiledValue()
    {
        assertEquals(5d, evaluate("d2:ceil(4.1)"));
        assertEquals(1d, evaluate("d2:ceil(0.8)"));
        assertEquals(6d, evaluate("d2:ceil(5.1)"));
        assertEquals(1d, evaluate("d2:ceil(1)"));
        assertEquals(-9d, evaluate("d2:ceil(-9.3)"));
        assertEquals(-5d, evaluate("d2:ceil(-5.9)"));
    }

    @Test
    void return_zero_when_number_is_invalid()
    {
        // ANTLR would return 0
        assertThrows(IllegalExpressionException.class, () -> evaluate("d2:ceil('str')"));
    }

    @Test
    void return_NaN_when_input_is_NaN()
    {
        assertEquals(Double.NaN, evaluate("d2:ceil(1%0)"));
    }

    private static double evaluate(String expression) {
        return (double) new Expression(expression, Expression.Mode.RULE_ENGINE).evaluate();
    }
}
