package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test of the `d2:split` function.
 *
 * @author Jan Bernitt
 */
internal class SplitTest {

    @Test
    fun testSplit_Null() {
        assertEquals("", evaluate("d2:split(null,\"\",0)"))
        assertEquals("", evaluate("d2:split(\"\",null,0)"))
        assertEquals("", evaluate("d2:split(\"aba\",\"b\",null)"))
    }

    @Test
    fun testSplit_Empty() {
        assertEquals("", evaluate("d2:split(\"\",\"x\",0)"))
        assertEquals("", evaluate("d2:split(\"x\",\"\",0)"))
    }

    @Test
    fun testSplit_IndexInBounds() {
        assertEquals("aa", evaluate("d2:split(\"aababaaab\",\"b\",0)"))
        assertEquals("a", evaluate("d2:split(\"aababaaab\",\"b\",1)"))
        assertEquals("aaa", evaluate("d2:split(\"aababaaab\",\"b\",2)"))
    }

    @Test
    fun testSplit_IndexOutOfBounds() {
        assertEquals("", evaluate("d2:split(\"aababaaab\",\"b\",3)"))
        assertEquals("", evaluate("d2:split(\"aababaaab\",\"b\",-1)"))
    }

    private fun evaluate(expression: String): String? {
        return Expression(expression, Expression.Mode.RULE_ENGINE_ACTION).evaluate() as String?
    }
}