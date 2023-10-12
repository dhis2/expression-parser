package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the `log10` function
 *
 * @author Jan Bernitt
 */
internal class Log10Test {

    @Test
    fun testLog() {
        assertEquals(Double.NEGATIVE_INFINITY, evaluate("log10(0)"))
        assertEquals(0.0, evaluate("log10(1)"))
        assertEquals(1.0, evaluate("log10(10)"))
    }

    private fun evaluate(expression: String): Any? {
        return Expression(expression).evaluate()
    }
}