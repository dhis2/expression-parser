package org.hisp.dhis.expression;

import org.hisp.dhis.expression.ast.NamedFunction;
import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.eval.CalcNodeInterpreter;
import org.hisp.dhis.expression.parse.ExprGrammar;
import org.hisp.dhis.expression.parse.FragmentContext;
import org.hisp.dhis.expression.parse.NamedFragments;
import org.hisp.dhis.expression.parse.Parser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link NamedFunction}s.
 */
class NamedFunctionExpressionTest {

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

    private static final NamedFragments FRAGMENTS = new FragmentContext(ExprGrammar.Constants, ExprGrammar.Functions, ExprGrammar.Modifiers);

    private Object evaluate(String expression) {
        Node<?> root = Parser.parse(expression, FRAGMENTS);
        return root.eval(new CalcNodeInterpreter());
    }
}
