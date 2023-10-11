package org.hisp.dhis.lib.expression

import kotlin.test.assertEquals
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * Port of the ANTLR `LogicalExpressionTest` and some more.
 */
internal class LogicalExpressionTest {
    @Test
    fun testNumericLiteralsAsBooleans() {
        assertEquals(true, evaluate("1 and 2"))
        assertEquals(true, evaluate("1 && 2"))
        assertEquals(false, evaluate("0 && 1"))
        assertEquals(false, evaluate("0 && 1"))
        assertEquals(false, evaluate("0 && 0"))
        assertEquals(false, evaluate("1.0 && 0"))
    }

    @Test
    fun testDoubleAsBoolean() {
        val ex = assertFailsWith(IllegalArgumentException::class) { evaluate("1.1 and 2") }
        assertEquals(
            "Failed to coerce value '1.1' (Double) to Boolean: Could not coerce Double '1.1' to Boolean\n" +
                    "\t in expression: 1.1", ex.message)
    }

    @Test
    fun testAnd() {
        assertEquals(true, evaluate("true and true"))
        assertEquals(true, evaluate("true && true"))
        assertEquals(false, evaluate("true && false"))
        assertEquals(false, evaluate("false && true"))
        assertEquals(false, evaluate("false && false"))
    }

    @Test
    fun testOr() {
        assertEquals(true, evaluate("true or true"))
        assertEquals(true, evaluate("true || true"))
        assertEquals(true, evaluate("true || false"))
        assertEquals(true, evaluate("false || true"))
        assertEquals(false, evaluate("false || false"))
    }

    @Test
    fun testNot() {
        assertEquals(true, evaluate("!false"))
        assertEquals(false, evaluate("!true"))
        assertEquals(true, evaluate("not false"))
        assertEquals(false, evaluate("not true"))
    }

    @Test
    fun testEquality() {
        assertEquals(true, evaluate("true != false"))
        assertEquals(false, evaluate("true == false"))
    }

    companion object {
        private fun evaluate(expression: String): Boolean {
            return Expression(expression).evaluate() as Boolean
        }
    }
}
