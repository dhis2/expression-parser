package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test of the `d2:substring` function.
 *
 * @author Jan Bernitt
 */
internal class SubstringTest {

    @Test
    fun testSubstring_Null() {
        assertEquals("", evaluate("d2:substring(null,0,1)"))
    }

    @Test
    fun testSubstring_Empty() {
        assertEquals("", evaluate("d2:substring(\"\",0,1)"))
    }

    @Test
    fun testSubstring_IndexInBounds() {
        assertEquals("he", evaluate("d2:substring(\"hello\",0,2)"))
        assertEquals("el", evaluate("d2:substring(\"hello\",1,3)"))
        assertEquals("lo", evaluate("d2:substring(\"hello\",3,5)"))
    }

    @Test
    fun testSubstring_IndexOutOfBounds() {
        assertEquals("he", evaluate("d2:substring(\"hello\",-2,2)"))
        assertEquals("ello", evaluate("d2:substring(\"hello\",1,30)"))
    }

    private fun evaluate(expression: String): String? {
        return Expression(expression, Expression.Mode.RULE_ENGINE_ACTION).evaluate() as String?
    }
}