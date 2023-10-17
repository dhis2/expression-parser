package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test of the `d2:validatePattern` function.
 *
 * @author Jan Bernitt
 */
internal class ValidatePatternTest {

    @Test
    fun testValidatePattern_Null() {
        assertFalse(evaluate("d2:validatePattern(null, \"x\")"))
        assertFalse(evaluate("d2:validatePattern(\"x\", null)"))
    }

    @Test
    fun testValidatePattern_Empty() {
        assertFalse(evaluate("d2:validatePattern(null, \"\")"))
        assertFalse(evaluate("d2:validatePattern(\"\", null)"))
    }

    @Test
    fun testValidatePattern_Match() {
        assertTrue(evaluate("d2:validatePattern(\"124\", \"[0-9]+\")"))
        assertTrue(evaluate("d2:validatePattern(\"12x4\", \"[0-9x]+\")"))
    }

    @Test
    fun testValidatePattern_NoMatch() {
        assertFalse(evaluate("d2:validatePattern(\"12x4\", \"[0-9]+\")"))
        assertFalse(evaluate("d2:validatePattern(\"ab0\", \"[0-9x]+\")"))
    }

    private fun evaluate(expression: String): Boolean {
        return Expression(expression, Expression.Mode.RULE_ENGINE_ACTION).evaluate() as Boolean
    }
}