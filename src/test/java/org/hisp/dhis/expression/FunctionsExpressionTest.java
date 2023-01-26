package org.hisp.dhis.expression;

import org.hisp.dhis.expression.ast.NamedFunction;
import org.hisp.dhis.expression.spi.IllegalExpressionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the {@link NamedFunction}s.
 *
 * @author Jan Bernitt
 */
class FunctionsExpressionTest {

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

    @Test
    void testIsNotNull() {
        assertEquals(true, evaluate("isNotNull(2)"));
        assertEquals(true, evaluate("isNotNull(42.0)"));
        assertEquals(true, evaluate("isNotNull('hello')"));
        assertEquals(true, evaluate("isNotNull(false)"));
        assertEquals(true, evaluate("isNotNull(1%0)"));
        assertEquals(true, evaluate("isNotNull(1/0)"));
        assertEquals(false, evaluate("isNotNull(null)"));
    }

    @Test
    void testIsNull() {
        assertEquals(false, evaluate("isNull(2)"));
        assertEquals(false, evaluate("isNull(42.0)"));
        assertEquals(false, evaluate("isNull('hello')"));
        assertEquals(false, evaluate("isNull(false)"));
        assertEquals(false, evaluate("isNull(1%0)"));
        assertEquals(false, evaluate("isNull(1/0)"));
        assertEquals(true, evaluate("isNull(null)"));
    }

    @Test
    void testLeast() {
        assertEquals(0.4d, evaluate("least(3,2.2,5.0,0.4)") );
    }

    @Test
    void testLeast_Null() {
        assertEquals(4d, evaluate("least(null,4,5.0)"));
    }

    @Test
    void testLeast_NaN() {
        assertEquals(4d, evaluate("least(1%0,4,5.0)"));
    }

    @Test
    void testLeast_PositiveInfinity() {
        assertEquals(Double.NEGATIVE_INFINITY, evaluate("least(-1/0,4,5.0,1/0)"));
    }

    @Test
    void testLeast_Empty() {
        assertNull(evaluate("least()"));
    }

    @Test
    void testRemoveZeros() {
        assertNull(evaluate("removeZeros(0.0)"));
        assertNull(evaluate("removeZeros(0)"));
        assertEquals(42d, evaluate("removeZeros(42.0)"));
    }

    private static Object evaluate(String expression) {
        return new Expression(expression).evaluate();
    }
}
