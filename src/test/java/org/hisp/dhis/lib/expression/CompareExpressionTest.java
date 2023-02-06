package org.hisp.dhis.lib.expression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Port of the ANTLR {@code CompareExpressionTest}.
 */
class CompareExpressionTest {

    @Test
    void testGreaterForDifferentTypes() {
        assertEquals( true, evaluate( "'2' > 1" ) );
        assertEquals( false, evaluate( "1 > '2'" ) );
        assertEquals( false, evaluate( "'10' > '2'" ) );
        assertEquals( true, evaluate( "10 > '2'" ) );
        assertEquals( true, evaluate( "'1' > -2" ) );
        assertEquals( true, evaluate( "'-10' < -1" ) );
        assertEquals( false, evaluate( "'2' > ( 1 + 1 )" ) );
        assertEquals( true, evaluate( "true > 0" ) );
        assertEquals( false, evaluate( "false > 1" ) );
    }

    @Test
    void testGreater() {
        assertEquals( true, evaluate( "2 > 1" ) );
        assertEquals( false, evaluate( "1 > 2" ) );
        assertEquals( false, evaluate( "2 > ( 1 + 1 )" ) );
        assertEquals( true, evaluate( "true > false" ) );
        assertEquals( false, evaluate( "true > true" ) );
        assertEquals( false, evaluate( "'abc' > 'abc'" ) );
        assertEquals( false, evaluate( "'abc' > 'def'" ) );
    }

    @Test
    void testGreaterOrEqual() {
        assertEquals( true, evaluate( "2 >= 1" ) );
        assertEquals( false, evaluate( "1 >= 2" ) );
        assertEquals( true, evaluate( "2 >= ( 1 + 1 )" ) );
        assertEquals( true, evaluate( "true >= false" ) );
        assertEquals( true, evaluate( "true >= true" ) );
        assertEquals( true, evaluate( "'abc' >= 'abc'" ) );
        assertEquals( false, evaluate( "'abc' >= 'def'" ) );
    }

    @Test
    void testLess() {
        assertEquals( false, evaluate( "2 < 1" ) );
        assertEquals( true, evaluate( "1 < 2" ) );
        assertEquals( false, evaluate( "2 < ( 1 + 1 )" ) );
        assertEquals( false, evaluate( "true < false" ) );
        assertEquals( false, evaluate( "true < true" ) );
        assertEquals( false, evaluate( "'abc' < 'abc'" ) );
        assertEquals( true, evaluate( "'abc' < 'def'" ) );
    }

    @Test
    void testLessOrEqual() {
        assertEquals( false, evaluate( "2 <= 1" ) );
        assertEquals( true, evaluate( "1 <= 2" ) );
        assertEquals( true, evaluate( "2 <= ( 1 + 1 )" ) );
        assertEquals( false, evaluate( "true <= false" ) );
        assertEquals( true, evaluate( "true <= true" ) );
        assertEquals( true, evaluate( "'abc' <= 'abc'" ) );
        assertEquals( true, evaluate( "'abc' <= 'def'" ) );
    }

    @Test
    void testEqual() {
        assertEquals( false, evaluate( "2 == 1" ) );
        assertEquals( false, evaluate( "1 == 2" ) );
        assertEquals( true, evaluate( "2 == ( 1 + 1 )" ) );
    }

    @Test
    void testNotEqual() {
        assertEquals( true, evaluate( "2 != 1" ) );
        assertEquals( true, evaluate( "1 != 2" ) );
        assertEquals( false, evaluate( "2 != ( 1 + 1 )" ) );
    }

    @Test
    void testCompareDifferentTypes() {
        assertEquals( true, evaluate( "2 == '2'" ) );
        assertEquals( true, evaluate( "'2' == 2" ) );
        assertEquals( true, evaluate( "'hi' == \"hi\"" ) );
        assertEquals( true, evaluate( "'true' == true" ) );
        assertEquals( true, evaluate( "true == 'true'" ) );
    }

    @Test
    void testDivideByZero() {
        assertEquals( false, evaluate( "2 == 2 / 0" ) );
    }

    @Test
    void testIncompatibleTypes() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> evaluate("2.1 == false"));
        assertEquals("Could not coerce Double '2.1' to Boolean", ex.getMessage());
    }

    private static Object evaluate(String expression) {
        return new Expression(expression).evaluate();
    }
}
