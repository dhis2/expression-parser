package org.hisp.dhis.expression;

import org.hisp.dhis.expression.ast.NamedFunction;
import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.eval.CalcNodeInterpreter;
import org.hisp.dhis.expression.parse.ExpressionGrammar;
import org.hisp.dhis.expression.parse.Parser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private static Object evaluate(String expression) {
        Node<?> root = Parser.parse(expression, ExpressionGrammar.Fragments);
        return root.eval(new CalcNodeInterpreter());
    }
}
