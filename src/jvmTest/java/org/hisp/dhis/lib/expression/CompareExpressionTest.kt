package org.hisp.dhis.lib.expression

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test

/**
 * Port of the ANTLR `CompareExpressionTest`.
 */
internal class CompareExpressionTest {
    @Test
    fun testGreaterForDifferentTypes() {
        assertEquals(true, evaluate("'2' > 1"))
        assertEquals(false, evaluate("1 > '2'"))
        assertEquals(false, evaluate("'10' > '2'"))
        assertEquals(true, evaluate("10 > '2'"))
        assertEquals(true, evaluate("'1' > -2"))
        assertEquals(true, evaluate("'-10' < -1"))
        assertEquals(false, evaluate("'2' > ( 1 + 1 )"))
        assertEquals(true, evaluate("true > 0"))
        assertEquals(false, evaluate("false > 1"))
    }

    @Test
    fun testGreater() {
        assertEquals(true, evaluate("2 > 1"))
        assertEquals(false, evaluate("1 > 2"))
        assertEquals(false, evaluate("2 > ( 1 + 1 )"))
        assertEquals(true, evaluate("true > false"))
        assertEquals(false, evaluate("true > true"))
        assertEquals(false, evaluate("'abc' > 'abc'"))
        assertEquals(false, evaluate("'abc' > 'def'"))
    }

    @Test
    fun testGreaterOrEqual() {
        assertEquals(true, evaluate("2 >= 1"))
        assertEquals(false, evaluate("1 >= 2"))
        assertEquals(true, evaluate("2 >= ( 1 + 1 )"))
        assertEquals(true, evaluate("true >= false"))
        assertEquals(true, evaluate("true >= true"))
        assertEquals(true, evaluate("'abc' >= 'abc'"))
        assertEquals(false, evaluate("'abc' >= 'def'"))
    }

    @Test
    fun testLess() {
        assertEquals(false, evaluate("2 < 1"))
        assertEquals(true, evaluate("1 < 2"))
        assertEquals(false, evaluate("2 < ( 1 + 1 )"))
        assertEquals(false, evaluate("true < false"))
        assertEquals(false, evaluate("true < true"))
        assertEquals(false, evaluate("'abc' < 'abc'"))
        assertEquals(true, evaluate("'abc' < 'def'"))
    }

    @Test
    fun testLessOrEqual() {
        assertEquals(false, evaluate("2 <= 1"))
        assertEquals(true, evaluate("1 <= 2"))
        assertEquals(true, evaluate("2 <= ( 1 + 1 )"))
        assertEquals(false, evaluate("true <= false"))
        assertEquals(true, evaluate("true <= true"))
        assertEquals(true, evaluate("'abc' <= 'abc'"))
        assertEquals(true, evaluate("'abc' <= 'def'"))
    }

    @Test
    fun testEqual() {
        assertEquals(false, evaluate("2 == 1"))
        assertEquals(false, evaluate("1 == 2"))
        assertEquals(true, evaluate("2 == ( 1 + 1 )"))
    }

    @Test
    fun testNotEqual() {
        assertEquals(true, evaluate("2 != 1"))
        assertEquals(true, evaluate("1 != 2"))
        assertEquals(false, evaluate("2 != ( 1 + 1 )"))
    }

    @Test
    fun testCompareDifferentTypes() {
        assertEquals(true, evaluate("2 == '2'"))
        assertEquals(true, evaluate("'2' == 2"))
        assertEquals(true, evaluate("'hi' == \"hi\""))
        assertEquals(true, evaluate("'true' == true"))
        assertEquals(true, evaluate("true == 'true'"))
    }

    @Test
    fun testDivideByZero() {
        assertEquals(false, evaluate("2 == 2 / 0"))
    }

    @Test
    fun testIncompatibleTypes() {
        val ex = assertFailsWith(IllegalArgumentException::class) { evaluate("2.1 == false") }
        assertEquals("Could not coerce Double '2.1' to Boolean", ex.message)
    }

    companion object {
        private fun evaluate(expression: String): Any? {
            return Expression(expression).evaluate()
        }
    }
}
