package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.ExpressionMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests the `d2:yearsBetween` function
 *
 * @author Jan Bernitt
 */
internal class YearsBetweenTest {

    @Test
    fun testYearsBetween() {
        assertEquals(0, evaluate("d2:yearsBetween(\"2020-01-01\", \"2020-01-07\")"))
        assertEquals(0, evaluate("d2:yearsBetween(\"2020-01-07\", \"2020-01-01\")"))
        assertEquals(0, evaluate("d2:yearsBetween(\"2020-01-01\", \"2020-02-01\")"))
        assertEquals(0, evaluate("d2:yearsBetween(\"2020-02-01\", \"2020-03-01\")"))
        assertEquals(1, evaluate("d2:yearsBetween(\"2020-01-01\", \"2021-01-01\")"))
        assertEquals(1, evaluate("d2:yearsBetween(\"2021-01-01\", \"2022-01-01\")"))
    }

    @Test
    fun testYearsBetween_Null() {
        val ex = assertFailsWith(IllegalArgumentException::class) { evaluate("d2:yearsBetween(null, \"2021-01-01\")") }
        assertEquals("start parameter of d2:yearsBetween must not be null", ex.message)
        val ex2 = assertFailsWith(IllegalArgumentException::class) { evaluate("d2:yearsBetween(\"2021-01-01\", null)") }
        assertEquals("end parameter of d2:yearsBetween must not be null", ex2.message)
    }
    
    private fun evaluate(expression: String): Any? {
        return Expression(expression, ExpressionMode.RULE_ENGINE_ACTION).evaluate()
    }
}