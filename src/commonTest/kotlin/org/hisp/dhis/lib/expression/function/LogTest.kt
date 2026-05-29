package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.ExpressionMode
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

    @Test
    fun testD2Log() {
        assertEquals(3.0, Expression("d2:log(8, 2)", ExpressionMode.RULE_ENGINE_ACTION).evaluate())
    }

    private fun evaluate(expression: String): Any? {
        return Expression(expression).evaluate()
    }
}