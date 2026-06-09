package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.ExpressionMode
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the `d2:exponent` function
 *
 * @author Tony Valle
 */
internal class ExponentTest {

    @Test
    fun testExponent() {
        assertEquals(1.0, evaluate("d2:exponent(49, 0)"))
        assertEquals(7.0, evaluate("d2:exponent(49, 0.5)"))
        assertEquals(49.0, evaluate("d2:exponent(49, 1)"))
        assertEquals(64.0, evaluate("d2:exponent(32, 1.2)") as Double, errorMargin)
        assertEquals(25.0, evaluate("d2:exponent(0.2, -2)") as Double, errorMargin)
    }

    @Test
    fun testExponent_Whitespace() {
        assertEquals(2.0, evaluate("d2:exponent(2,1)"))
        assertEquals(2.0, evaluate("d2:exponent( 2, 1)"))
        assertEquals(2.0, evaluate("d2:exponent(2 , 1 )"))
        assertEquals(2.0, evaluate("d2:exponent( 2 , 1 )"))
    }

    private val errorMargin: Double = 0.00000001

    private fun evaluate(expression: String): Any? {
        return Expression(expression, ExpressionMode.RULE_ENGINE_ACTION).evaluate()
    }
}
