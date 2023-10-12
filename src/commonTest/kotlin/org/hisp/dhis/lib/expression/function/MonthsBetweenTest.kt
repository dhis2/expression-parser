package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests the `d2:monthsBetween` function
 *
 * @author Jan Bernitt
 */
internal class MonthsBetweenTest {

    @Test
    fun testMonthsBetween() {
        assertEquals(0, evaluate("d2:monthsBetween(\"2020-01-01\", \"2020-01-07\")"))
        assertEquals(0, evaluate("d2:monthsBetween(\"2020-01-07\", \"2020-01-01\")"))
        assertEquals(1, evaluate("d2:monthsBetween(\"2020-01-01\", \"2020-02-01\")"))
        assertEquals(1, evaluate("d2:monthsBetween(\"2020-02-01\", \"2020-03-01\")"))
        assertEquals(12, evaluate("d2:monthsBetween(\"2020-01-01\", \"2021-01-01\")"))
        assertEquals(12, evaluate("d2:monthsBetween(\"2021-01-01\", \"2020-01-01\")"))
    }

    @Test
    fun testMonthsBetween_Null() {
        val ex = assertFailsWith(IllegalArgumentException::class) { evaluate("d2:monthsBetween(null, \"2021-01-01\")") }
        assertEquals("start parameter of d2:monthsBetween must not be null", ex.message)
        val ex2 = assertFailsWith(IllegalArgumentException::class) { evaluate("d2:monthsBetween(\"2021-01-01\", null)") }
        assertEquals("end parameter of d2:monthsBetween must not be null", ex2.message)
    }
    
    private fun evaluate(expression: String): Any? {
        return Expression(expression, Expression.Mode.RULE_ENGINE_ACTION).evaluate()
    }
}