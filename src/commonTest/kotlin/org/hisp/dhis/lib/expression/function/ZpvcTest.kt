package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test of the `d2:zpvc` function.
 *
 * ZPVC is short for Zero or Positive Value Count
 *
 * @author Jan Bernitt
 */
internal class ZpvcTest {

    @Test
    fun testZpvc_Null() {
        assertEquals(0, evaluate("d2:zpvc(null)"))
    }

    @Test
    fun testZpvc_None() {
        assertEquals(0, evaluate("d2:zpvc()"))
    }

    @Test
    fun testZpvc_Negative() {
        assertEquals(0, evaluate("d2:zpvc(-1)"))
        assertEquals(0, evaluate("d2:zpvc(-1,-2.3)"))
        assertEquals(0, evaluate("d2:zpvc(-1,-2.3,-4.0004)"))
    }

    @Test
    fun testZpvc_Zero() {
        assertEquals(1, evaluate("d2:zpvc(+0)"))
        assertEquals(1, evaluate("d2:zpvc(0)"))
        assertEquals(1, evaluate("d2:zpvc(-0)"))
    }

    @Test
    fun testZpvc_Positive() {
        assertEquals(1, evaluate("d2:zpvc(1)"))
        assertEquals(2, evaluate("d2:zpvc(1,2.3)"))
        assertEquals(3, evaluate("d2:zpvc(1,2.3,4.0004)"))
    }

    @Test
    fun testZpvc_Mixed() {
        assertEquals(1, evaluate("d2:zpvc(1,-1)"))
        assertEquals(2, evaluate("d2:zpvc(1,-1,0)"))
    }

    private fun evaluate(expression: String): Any? {
        return Expression(expression, Expression.Mode.RULE_ENGINE_ACTION).evaluate()
    }
}