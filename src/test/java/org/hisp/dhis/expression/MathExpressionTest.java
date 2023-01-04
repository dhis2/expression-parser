package org.hisp.dhis.expression;

import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.eval.CalcNodeInterpreter;
import org.hisp.dhis.expression.parse.ExpressionGrammar;
import org.hisp.dhis.expression.parse.Parser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
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
    @Disabled("log not implemented yet")
    void testLog() {
        assertEquals( 4.605170, evaluate( "log(100)" ) );
        assertEquals( -0.693147, evaluate( "log( .5 )" ) );
        assertEquals( 3, evaluate( "log(8,2)" ) );
        assertEquals( 2, evaluate( "log(256, 16)" ) );
        assertEquals( 2, evaluate( "log( 100, 10 )" ) );
    }

    @Test
    @Disabled("log10 not implemented yet")
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
    }

    @Test
    void testModule() {
        assertEquals( 1.0, evaluate( "1%2" ) );
        assertEquals( 1.0, evaluate( "5%2" ) );
        assertEquals( 1.4, evaluate( "1.4%( 1 + 1 )" ) );
        assertEquals( Double.NaN, evaluate( "1.0%( 1 - 1 )" ) );
    }

    private static void assertEquals(double expected, Number actual) {
        Assertions.assertEquals(expected, actual.doubleValue());
    }

    private static Number evaluate(String expression) {
        Node<?> root = Parser.parse(expression, ExpressionGrammar.Fragments);
        return (Number) root.eval(new CalcNodeInterpreter());
    }
}
