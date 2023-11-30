package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests the `d2:daysBetween` function
 *
 * @author Jan Bernitt
 */
internal class DaysBetweenTest {

    @Test
    fun testDaysBetween() {
        assertEquals(6, evaluate("d2:daysBetween(\"2020-01-01\", \"2020-01-07\")"))
        assertEquals(31, evaluate("d2:daysBetween(\"2020-01-01\", \"2020-02-01\")"))
        assertEquals(29, evaluate("d2:daysBetween(\"2020-02-01\", \"2020-03-01\")"))
        assertEquals(366, evaluate("d2:daysBetween(\"2020-01-01\", \"2021-01-01\")"))
    }

    @Test
    fun testDaysBetween_Negative() {
        assertEquals(-6, evaluate("d2:daysBetween(\"2020-01-07\", \"2020-01-01\")"))
    }

    @Test
    fun testDaysBetween_Null() {
        val ex = assertFailsWith(IllegalArgumentException::class) { evaluate("d2:daysBetween(null, \"2021-01-01\")") }
        assertEquals("start parameter of d2:daysBetween must not be null", ex.message)
        val ex2 = assertFailsWith(IllegalArgumentException::class) { evaluate("d2:daysBetween(\"2021-01-01\", null)") }
        assertEquals("end parameter of d2:daysBetween must not be null", ex2.message)
    }

    private fun evaluate(expression: String): Any? {
        return Expression(expression, Expression.Mode.RULE_ENGINE_ACTION).evaluate()
    }
}