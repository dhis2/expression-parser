package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests the `d2:minutesBetween` function
 *
 * @author Jan Bernitt
 */
internal class MinutesBetweenTest {

    @Test
    fun testMinutesBetween() {
        val minPerDay = 60 * 24
        assertEquals(6 * minPerDay, evaluate("d2:minutesBetween(\"2020-01-01\", \"2020-01-07\")"))
        assertEquals(31 * minPerDay, evaluate("d2:minutesBetween(\"2020-01-01\", \"2020-02-01\")"))
        assertEquals(29 * minPerDay, evaluate("d2:minutesBetween(\"2020-02-01\", \"2020-03-01\")"))
        assertEquals(366 * minPerDay, evaluate("d2:minutesBetween(\"2020-01-01\", \"2021-01-01\")"))
    }

    @Test
    fun testMinutesBetween_Negative() {
        val minPerDay = 60 * 24
        assertEquals(-6 * minPerDay, evaluate("d2:minutesBetween(\"2020-01-07\", \"2020-01-01\")"))
    }

    @Test
    fun testMinutesBetween_Null() {
        val ex = assertFailsWith(IllegalArgumentException::class) { evaluate("d2:minutesBetween(null, \"2021-01-01\")") }
        assertEquals("start parameter of d2:minutesBetween must not be null", ex.message)
        val ex2 = assertFailsWith(IllegalArgumentException::class) { evaluate("d2:minutesBetween(\"2021-01-01\", null)") }
        assertEquals("end parameter of d2:minutesBetween must not be null", ex2.message)
    }

    private fun evaluate(expression: String): Any? {
        return Expression(expression, Expression.Mode.PROGRAM_INDICATOR_EXPRESSION).evaluate()
    }
}