package org.hisp.dhis.expression;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that program rule variables are identified and collected.
 *
 * @author Jan Bernitt
 */
class ProgramRuleVariableExpressionTest {

    @Test
    void testProgramRuleVariableString() {
        assertEquals(Set.of("var1"), evaluate("d2:count('var1')"));
    }

    @Test
    void testProgramRuleVariableName() {
        assertEquals(Set.of("var1"), evaluate("#{var1}"));
        assertEquals(Set.of("var1"), evaluate("A{var1}"));
    }

    private static Set<String> evaluate(String expression) {
        return new Expression(expression).collectProgramRuleVariables();
    }
}
