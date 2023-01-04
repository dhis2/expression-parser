package org.hisp.dhis.expression;

import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.eval.CalcNodeInterpreter;
import org.hisp.dhis.expression.parse.Expr.ParseException;
import org.hisp.dhis.expression.parse.ExpressionGrammar;
import org.hisp.dhis.expression.parse.Parser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Port of the ANTLR {@code ValidExpressionTest}.
 */
class ValidExpressionTest {

    @Test
    void testValidExpression() {
        assertEquals( "a", evaluate( "'a'" ) );
        assertEquals( true, evaluate( "true" ) );
        assertEquals( "true", evaluate( "'true'" ) );
        assertEquals( 34.0, evaluate( "34" ) );
        assertEquals( "34", evaluate( "'34'" ) );
        assertEquals( "34.0", evaluate( "'34.0'" ) );
    }

    @Test
    void testInvalidSyntaxExpression1()
    {
        ParseException ex = assertThrows( ParseException.class, () ->  evaluate( "1+1+1 > 0 _ 1" ));
        assertEquals("Invalid string token '_' at line:1 character:10", ex.getMessage() );
    }

    @Test
    void testInvalidSyntaxExpression2()
    {
        ParseException ex = assertThrows( ParseException.class, () -> evaluate( "1 + 1 + a" ));
        assertEquals("Invalid string token 'a' at line:1 character:8", ex.getMessage() );
    }

    @Test
    void testInvalidSyntaxExpression3()
    {
        ParseException ex = assertThrows( ParseException.class, () -> evaluate( "1 + 1  ( 2 + 2 )" ));
        assertEquals("Invalid string token '(' at line:1 character:7", ex.getMessage() );
    }

    @Test
    void testExpressionWithValidSyntaxAndNotSupportedItem()
    {
        assertThrows( ParseException.class, () -> evaluate( "2 > #{not_supported}" ));
    }

    @Test
    void testExpressionWithValidSyntaxAndNotSupportedVariable()
    {
        assertThrows( ParseException.class, () -> evaluate( "2 > V{not_supported}" ));
    }

    @Test
    void testExpressionWithValidSyntaxAndNotSupportedAttribute()
    {
        assertThrows( ParseException.class, () -> evaluate( "2 > A{not_supported}" ));
    }

    private static Object evaluate( String expression )
    {
        Node<?> root = Parser.parse(expression, ExpressionGrammar.Fragments);
        return root.eval(new CalcNodeInterpreter());
    }
}
