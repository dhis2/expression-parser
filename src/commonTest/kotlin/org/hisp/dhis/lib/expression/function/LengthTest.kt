package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test of the `d2:length` function.
 *
 * @author Jan Bernitt
 */
internal class LengthTest {

    @Test
    fun testLength() {
        assertEquals(0, evaluate("d2:length(\"\")"))
        assertEquals(5, evaluate("d2:length(\"hello\")"))
    }

    @Test
    fun testLength_Number() {
        assertEquals(1, evaluate("d2:length(0)"))
    }

    @Test
    fun testLength_Boolean() {
        assertEquals(4, evaluate("d2:length(true)"))
    }

    @Test
    fun testLength_Null() {
        assertEquals(0, evaluate("d2:length(null)"))
    }

    private fun evaluate(expression: String): Any? {
        return Expression(expression, Expression.Mode.RULE_ENGINE_ACTION).evaluate()
    }
}