package org.hisp.dhis.expression;

import org.hisp.dhis.expression.ast.NamedFunction;
import org.hisp.dhis.expression.spi.IllegalExpressionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the "base function" subset of the {@link NamedFunction}s.
 *
 * @author Jan Bernitt
 */
class BaseFunctionsExpressionTest {

    @Test
    void testFirstNonNull_Numbers() {
        assertEquals(42.0d, evaluate("firstNonNull(null, 42, 23)"));
    }

    @Test
    void testFirstNonNull_Strings() {
        assertEquals("42", evaluate("firstNonNull(null,null, \"42\", \"23\")"));
    }

    @Test
    void testFirstNonNull_Booleans() {
        assertEquals(true, evaluate("firstNonNull(true, null, false)"));
    }

    @Test
    void testGreatest() {
        assertEquals(5d, evaluate("greatest(3,2.2,5.0,0.4)") );
    }

    @Test
    void testGreatest_Null() {
        assertEquals(5d, evaluate("greatest(null,4,5.0)"));
    }

    @Test
    void testGreatest_NaN() {
        assertEquals(Double.NaN, evaluate("greatest(1%0,4,5.0)"));
    }

    @Test
    void testGreatest_PositiveInfinity() {
        assertEquals(Double.POSITIVE_INFINITY, evaluate("greatest(1/0,4,5.0)"));
    }

    @Test
    void testGreatest_Empty() {
        assertNull(evaluate("greatest()"));
    }

    @Test
    void testIf_True() {
        assertEquals(1d, evaluate("if(true, 1, 0)"));
        assertEquals(true, evaluate("if(1 < 4, true, false)"));
        assertNull(evaluate("if(1, null, 42)"));
    }

    @Test
    void testIf_False() {
        assertNull(evaluate("if(false, 1, null)"));
        assertEquals(false, evaluate("if(1 > 4, true, false)"));
        assertEquals(42d, evaluate("if(0, null, 42)"));
    }

    @Test
    void testIf_Null() {
        assertEquals(false, evaluate("if(null, true, false)"));
    }

    @Test
    void testIf_NaN() {
        IllegalExpressionException ex = assertThrows(IllegalExpressionException.class,
                () -> evaluate("if(1%0, true, false)"));
        assertEquals("Could not coerce Double 'NaN' to Boolean\n\t at: 1%0", ex.getMessage());
    }

    private static Object evaluate(String expression) {
        return new Expression(expression).evaluate();
    }
}
