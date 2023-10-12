package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the `log` function
 *
 * @author Jan Bernitt
 */
internal class LogTest {

    @Test
    fun testLog() {
        assertEquals(Double.NEGATIVE_INFINITY, evaluate("log(0)"))
        assertEquals(0.0, evaluate("log(1)"))
    }

    @Test
    fun testLog_Whitespace() {
        assertEquals(Double.POSITIVE_INFINITY, evaluate("log(2,1)"))
        assertEquals(Double.POSITIVE_INFINITY, evaluate("log( 2, 1)"))
        assertEquals(Double.POSITIVE_INFINITY, evaluate("log(2 , 1 )"))
        assertEquals(Double.POSITIVE_INFINITY, evaluate("log( 2 , 1 )"))
    }

    private fun evaluate(expression: String): Any? {
        return Expression(expression).evaluate()
    }
}