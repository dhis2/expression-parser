package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.ExpressionMode
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test of the `d2:oizp` function.
 *
 * OIZP is short for Object Is Zero or Positive
 *
 * @author Jan Bernitt
 */
internal class OizpTest {

    @Test
    fun testOizp_Null() {
        assertEquals(0.0, evaluate("d2:oizp(null)"))
    }

    @Test
    fun testOizp_NaN() {
        assertEquals(0.0, evaluate("d2:oizp(1%0)"))
    }

    @Test
    fun testOizp_Infinite() {
        assertEquals(1.0, evaluate("d2:oizp(1/0)"))
    }

    @Test
    fun testOizp_Zero() {
        assertEquals(1.0, evaluate("d2:oizp(0)"))
        assertEquals(1.0, evaluate("d2:oizp(0.0)"))
    }

    @Test
    fun testOizp_Positive() {
        assertEquals(1.0, evaluate("d2:oizp(0.0000001)"))
        assertEquals(1.0, evaluate("d2:oizp(42)"))
        assertEquals(1.0, evaluate("d2:oizp(42.13)"))
    }

    @Test
    fun testOizp_Negative() {
        assertEquals(0.0, evaluate("d2:oizp(-1)"))
        assertEquals(0.0, evaluate("d2:oizp(-0.0000001)"))
    }

    private fun evaluate(expression: String): Double? {
        return Expression(expression, ExpressionMode.RULE_ENGINE_ACTION).evaluate() as Double?
    }
}