package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.ExpressionMode
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test of the `d2:right` function.
 *
 * @author Jan Bernitt
 */
internal class RightTest {

    @Test
    fun testRight_Null() {
        assertEquals("", evaluate("d2:right(null, 4)"))
        assertEquals("", evaluate("d2:right(\"hello\", null)"))
    }

    @Test
    fun testRight_Empty() {
        assertEquals("", evaluate("d2:right(\"\", 1)"))
        assertEquals("", evaluate("d2:right(\"hello\", 0)"))
    }

    @Test
    fun testRight_InBounds() {
        assertEquals("o", evaluate("d2:right(\"hello\", 1)"))
        assertEquals("lo", evaluate("d2:right(\"hello\", 2)"))
        assertEquals("llo", evaluate("d2:right(\"hello\", 3)"))
        assertEquals("ello", evaluate("d2:right(\"hello\", 4)"))
        assertEquals("hello", evaluate("d2:right(\"hello\", 5)"))
    }

    @Test
    fun testRight_OutOfBounds() {
        assertEquals("hello", evaluate("d2:right(\"hello\", 10)"))
    }

    private fun evaluate(expression: String): String? {
        return Expression(expression, ExpressionMode.RULE_ENGINE_ACTION).evaluate() as String?
    }
}