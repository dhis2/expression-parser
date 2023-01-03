package org.hisp.dhis.expression;

import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.parse.ExprGrammar;
import org.hisp.dhis.expression.parse.FragmentContext;
import org.hisp.dhis.expression.parse.NamedFragments;
import org.hisp.dhis.expression.parse.Parser;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProgramRuleVariableTest {

    @Test
    void testProgramRuleVariableString() {
        assertEquals(Set.of("var1"), evaluate("d2:count('var1')"));
    }

    @Test
    void testProgramRuleVariableName() {
        assertEquals(Set.of("var1"), evaluate("#{var1}"));
    }

    private static final NamedFragments FRAGMENTS = new FragmentContext(ExprGrammar.Constants, ExprGrammar.Functions, ExprGrammar.Modifiers);

    private Set<String> evaluate(String expression) {
        Node<?> root = Parser.parse(expression, FRAGMENTS);
        Node.groupOperators(root);
        return Node.collectProgramRuleVariables(root);
    }
}
