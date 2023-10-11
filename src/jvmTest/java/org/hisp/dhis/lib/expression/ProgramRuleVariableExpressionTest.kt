package org.hisp.dhis.lib.expression

import org.hisp.dhis.lib.expression.Expression.Mode

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests that program rule variables are identified and collected.
 *
 * @author Jan Bernitt
 */
internal class ProgramRuleVariableExpressionTest {
    @Test
    fun testProgramRuleVariableString() {
        assertEquals(setOf("var1"), evaluate("d2:count('var1')"))
    }

    @Test
    fun testProgramRuleVariableName() {
        assertEquals(setOf("var1"), evaluate("#{var1}"))
        assertEquals(setOf("var1"), evaluate("A{var1}"))
    }

    companion object {
        private fun evaluate(expression: String): Set<String> {
            return Expression(expression, Mode.RULE_ENGINE_ACTION).collectProgramRuleVariableNames()
        }
    }
}
