package org.hisp.dhis.lib.expression

import org.hisp.dhis.lib.expression.spi.IllegalExpressionException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests the [UnaryOperator] evaluation.
 */
internal class UnaryOperatorExpressionTest {
    @Test
    fun testUnaryNot() {
        assertEquals(true, evaluate("!false"))
        assertEquals(false, evaluate("!true"))
        assertEquals(false, evaluate("not (true or false)"))
        assertEquals(true, evaluate("not(true and false)"))
    }

    @Test
    fun testUnaryPlus() {
        assertEquals(10.0, evaluate("++10"))
        assertEquals(8.0, evaluate("+(+10-2)"))
    }

    @Test
    fun testUnaryMinus() {
        assertEquals(-10.0, evaluate("-10"))
        assertEquals(10.0, evaluate("--10"))
        assertEquals(-8.0, evaluate("-(+10-2)"))
    }

    @Test
    fun testUnaryDistinct() {
        val ex = assertFailsWith(IllegalExpressionException::class) { evaluate("distinct 1") }
        assertEquals("Unary operator not supported for direct evaluation: DISTINCT", ex.message)
    }

    companion object {
        private fun evaluate(expression: String): Any? {
            return Expression(expression).evaluate()
        }
    }
}
