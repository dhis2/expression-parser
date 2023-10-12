package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests the `d2:weeksBetween` function
 *
 * @author Jan Bernitt
 */
internal class WeeksBetweenTest {

    @Test
    fun testWeeksBetween() {
        assertEquals(0, evaluate("d2:weeksBetween(\"2020-01-01\", \"2020-01-07\")"))
        assertEquals(0, evaluate("d2:weeksBetween(\"2020-01-07\", \"2020-01-01\")"))
        assertEquals(4, evaluate("d2:weeksBetween(\"2020-01-01\", \"2020-02-01\")"))
        assertEquals(4, evaluate("d2:weeksBetween(\"2020-02-01\", \"2020-03-01\")"))
        assertEquals(52, evaluate("d2:weeksBetween(\"2020-01-01\", \"2021-01-01\")"))
        assertEquals(52, evaluate("d2:weeksBetween(\"2021-01-01\", \"2020-01-01\")"))
    }

    @Test
    fun testWeeksBetween_Null() {
        val ex = assertFailsWith(IllegalArgumentException::class) { evaluate("d2:weeksBetween(null, \"2021-01-01\")") }
        assertEquals("start parameter of d2:weeksBetween must not be null", ex.message)
        val ex2 = assertFailsWith(IllegalArgumentException::class) { evaluate("d2:weeksBetween(\"2021-01-01\", null)") }
        assertEquals("end parameter of d2:weeksBetween must not be null", ex2.message)
    }
    
    private fun evaluate(expression: String): Any? {
        return Expression(expression, Expression.Mode.RULE_ENGINE_ACTION).evaluate()
    }
}