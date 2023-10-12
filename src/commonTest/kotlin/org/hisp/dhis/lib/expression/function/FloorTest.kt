package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the `d2:floor` function
 *
 * @author Jan Bernitt
 */
internal class FloorTest {

    @Test
    fun testFloor() {
        assertEquals(1.0, evaluate("d2:floor(1.2)"))
        assertEquals(1.0, evaluate("d2:floor(1.5)"))
        assertEquals(1.0, evaluate("d2:floor(1.6)"))
        assertEquals(1.0, evaluate("d2:floor(1.9)"))
        assertEquals(2.0, evaluate("d2:floor(2.0)"))

    }

    @Test
    fun testFloor_NegativeNumber() {
        assertEquals(-2.0, evaluate("d2:floor(-1.2)"))
    }

    @Test
    fun testFloor_Null() {
        assertEquals(0.0, evaluate("d2:floor(null)"))
    }

    private fun evaluate(expression: String): Any? {
        return Expression(expression, Expression.Mode.RULE_ENGINE_ACTION).evaluate()
    }
}