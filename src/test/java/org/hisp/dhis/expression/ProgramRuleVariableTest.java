package org.hisp.dhis.expression;

import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.syntax.ExpressionGrammar;
import org.hisp.dhis.expression.syntax.Parser;
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

    private static Set<String> evaluate(String expression) {
        Node<?> root = Parser.parse(expression, ExpressionGrammar.Fragments);
        Node.groupOperators(root);
        return Node.collectProgramRuleVariables(root);
    }
}
