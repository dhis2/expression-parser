package org.hisp.dhis.lib.expression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Port of the ANTLR {@code LogicalExpressionTest} and some more.
 */
class LogicalExpressionTest {

    @Test
    void testNumericLiteralsAsBooleans()
    {
        assertEquals( true, evaluate( "1 and 2" ) );
        assertEquals( true, evaluate( "1 && 2" ) );
        assertEquals( false, evaluate( "0 && 1" ) );
        assertEquals( false, evaluate( "0 && 1" ) );
        assertEquals( false, evaluate( "0 && 0" ) );
        assertEquals( false, evaluate( "1.0 && 0" ) );
    }

    @Test
    void testDoubleAsBoolean() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> evaluate("1.1 and 2"));
        assertEquals("Failed to coerce value '1.1' (Double) to Boolean: Could not coerce Double '1.1' to Boolean\n" +
                "\t in expression: 1.1", ex.getMessage());
    }

    @Test
    void testAnd() {
        assertEquals( true, evaluate( "true and true" ) );
        assertEquals( true, evaluate( "true && true" ) );
        assertEquals( false, evaluate( "true && false" ) );
        assertEquals( false, evaluate( "false && true" ) );
        assertEquals( false, evaluate( "false && false" ) );
    }

    @Test
    void testOr() {
        assertEquals( true, evaluate( "true or true" ) );
        assertEquals( true, evaluate( "true || true" ) );
        assertEquals( true, evaluate( "true || false" ) );
        assertEquals( true, evaluate( "false || true" ) );
        assertEquals( false, evaluate( "false || false" ) );
    }

    @Test
    void testNot() {
        assertEquals( true, evaluate( "!false" ) );
        assertEquals( false, evaluate( "!true" ) );
        assertEquals( true, evaluate( "not false" ) );
        assertEquals( false, evaluate( "not true" ) );
    }

    @Test
    void testEquality() {
        assertEquals(true, evaluate("true != false"));
        assertEquals(false, evaluate("true == false"));
    }

    private static Boolean evaluate(String expression) {
        return (Boolean) new Expression(expression).evaluate();
    }
}
