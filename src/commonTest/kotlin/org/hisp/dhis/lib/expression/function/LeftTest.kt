package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.ExpressionMode
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test of the `d2:left` function.
 *
 * @author Jan Bernitt
 */
internal class LeftTest {

    @Test
    fun testLeft_Null() {
        assertEquals("", evaluate("d2:left(null, 4)"))
        assertEquals("", evaluate("d2:left(\"hello\", null)"))
    }

    @Test
    fun testLeft_Empty() {
        assertEquals("", evaluate("d2:left(\"\", 1)"))
        assertEquals("", evaluate("d2:left(\"hello\", 0)"))
    }

    @Test
    fun testLeft_InBounds() {
        assertEquals("h", evaluate("d2:left(\"hello\", 1)"))
        assertEquals("he", evaluate("d2:left(\"hello\", 2)"))
        assertEquals("hel", evaluate("d2:left(\"hello\", 3)"))
        assertEquals("hell", evaluate("d2:left(\"hello\", 4)"))
        assertEquals("hello", evaluate("d2:left(\"hello\", 5)"))
    }

    @Test
    fun testLeft_OutOfBounds() {
        assertEquals("hello", evaluate("d2:left(\"hello\", 10)"))
    }

    private fun evaluate(expression: String): String? {
        return Expression(expression, ExpressionMode.RULE_ENGINE_ACTION).evaluate() as String?
    }
}