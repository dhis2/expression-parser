package org.hisp.dhis.expression;

import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.ast.UnaryOperator;
import org.hisp.dhis.expression.eval.CalcNodeInterpreter;
import org.hisp.dhis.expression.parse.ExprGrammar;
import org.hisp.dhis.expression.parse.FragmentContext;
import org.hisp.dhis.expression.parse.NamedFragments;
import org.hisp.dhis.expression.parse.Parser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link UnaryOperator} evaluation.
 */
class UnaryOperatorExpressionTest {

    @Test
    void testUnaryNot() {
        assertEquals(true, evaluate("!false"));
        assertEquals(false, evaluate("!true"));
        assertEquals(false, evaluate("not (true or false)"));
        assertEquals(true, evaluate("not(true and false)"));
    }

    @Test
    void testUnaryPlus() {
        assertEquals(10.0d, evaluate("++10"));
        assertEquals(8.0d, evaluate("+(+10-2)"));
    }

    @Test
    void testUnaryMinus() {
        assertEquals(-10.0d, evaluate("-10"));
        assertEquals(10.0d, evaluate("--10"));
        assertEquals(-8.0d, evaluate("-(+10-2)"));
    }

    @Test
    void testUnaryDistinct() {
        //TODO
    }

    private static final NamedFragments FRAGMENTS = new FragmentContext(ExprGrammar.Constants, ExprGrammar.Functions, ExprGrammar.Modifiers);

    private Object evaluate(String expression) {
        Node<?> root = Parser.parse(expression, FRAGMENTS);
        return root.eval(new CalcNodeInterpreter());
    }
}
