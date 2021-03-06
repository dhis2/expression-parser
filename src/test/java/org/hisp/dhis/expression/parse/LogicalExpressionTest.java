package org.hisp.dhis.expression.parse;

import org.hisp.dhis.expression.EvaluateNodeTransformer;
import org.hisp.dhis.expression.Node;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertEquals("Could not cast Double '1.1' to Boolean", ex.getMessage());
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

    private static final NamedContext NAMED_CONTEXT = new DefaultNamedContext(ExprGrammar.Constants, ExprGrammar.Functions, ExprGrammar.Methods);

    private Boolean evaluate(String expression) {
        Node<?> root = Parser.parse(expression, NAMED_CONTEXT);
        return (Boolean) root.eval(new EvaluateNodeTransformer());
    }
}
