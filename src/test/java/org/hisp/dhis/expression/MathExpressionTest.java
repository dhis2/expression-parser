package org.hisp.dhis.expression;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Port of the ANTLR {@code MathExpressionTest}.
 */
class MathExpressionTest {
    @Test
    void testSum() {
        assertEquals( 2, evaluate( "1 + 1" ) );
        assertEquals( 2.3, evaluate( "1.3 + 1" ) );
        assertEquals( 3.3, evaluate( "1.3 + ( 1 + 1 )" ) );
    }

    @Test
    void testSub() {
        assertEquals( 0, evaluate( "+ 1 - 1" ) );
        assertEquals( 0.3, evaluate( "1.3 - 1" ) );
        assertEquals( -0.7, evaluate( "1.3 - ( 1 + 1 )" ) );
    }

    @Test
    void testDivide() {
        assertEquals( 3.333333333333333d, evaluate( "10/3" ) );
        assertEquals( 1.0, evaluate( "1/1" ) );
        assertEquals( 0.5, evaluate( "1/2" ) );
        assertEquals( -0.7, evaluate( "-1.4/( 1 + 1 )" ) );
        assertEquals( 1.0, evaluate( "( 1 / 1000 ) * 1000" ) );
        assertEquals( Double.POSITIVE_INFINITY, evaluate( "1.0/( 1 - 1 )" ) );
        assertEquals( Double.POSITIVE_INFINITY, evaluate( "80 + 4 / 0" ) );
    }

    @Test
    void testMultiply() {
        assertEquals( 1.0, evaluate( "1*1" ) );
        assertEquals( 2.0, evaluate( "1*2" ) );
        assertEquals( -2.8, evaluate( "-1.4*( 1 + 1 )" ) );
    }

    @Test
    void testLog() {
        assertEquals( 4.605170, evaluate( "log(100)" ) );
        assertEquals( -0.693147, evaluate( "log( .5 )" ) );
        assertEquals( 3, evaluate( "log(8,2)" ) );
        assertEquals( 2, evaluate( "log(256, 16)" ) );
        assertEquals( 2, evaluate( "log( 100, 10 )" ) );
    }

    @Test
    void testLog10() {
        assertEquals( 2, evaluate( "log10(100)" ) );
        assertEquals( -0.301030, evaluate( "log10( .5 )" ) );
    }

    @Test
    void testPower() {
        assertEquals( 1.0, evaluate( "1^10" ) );
        assertEquals( 25.0, evaluate( "5^2" ) );
        assertEquals( 1.96, evaluate( "1.4^( 1 + 1 )" ) );
        assertEquals( 1.0, evaluate( "1.4^( 1 - 1 )" ) );
        assertEquals( 4.0, evaluate( "2^2" ) );
        assertEquals( 0.25, evaluate( "2^-2" ) );
        assertEquals( 4.0, evaluate( "2^--2" ) );
        assertEquals( 16.0, evaluate( "2^2^2" ) );
        assertEquals( 9.0, evaluate( "(-3)^2" ) );
        assertEquals( -27.0, evaluate( "(-3)^3" ) );
    }

    @Test
    void testModulo() {
        assertEquals( 1.0, evaluate( "1%2" ) );
        assertEquals( 1.0, evaluate( "5%2" ) );
        assertEquals( 1.4, evaluate( "1.4%( 1 + 1 )" ) );
        assertEquals( Double.NaN, evaluate( "1.0%( 1 - 1 )" ) );
    }

    @Test
    void testBracketsAndOperatorPrecedence() {
        assertEquals(-2.25141952945498598E18, evaluate("(1+2)*-3^(9*4)*5+6"));
        assertEquals(-787313d, evaluate("1+2*-3^9*4*5+6"));
    }

    private static void assertEquals(double expected, Number actual) {
        Assertions.assertEquals(expected, actual.doubleValue(), 0.000001d);
    }

    private static Number evaluate(String expression) {
        return (Number) new Expression(expression).evaluate();
    }
}
