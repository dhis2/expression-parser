package org.hisp.dhis.lib.expression.operator

import org.hisp.dhis.lib.expression.Expression
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

        // Any value that cannot be coerced to a number is not equal to any number
        assertEquals(false, evaluate("2 == ''"))
        assertEquals(false, evaluate("2 == 'any string that is not a number'"))

        // Booleans are coerced to numbers (true→1, false→0) when compared with a number
        assertEquals(true, evaluate("1 == true"))
        assertEquals(true, evaluate("0 == false"))
        assertEquals(false, evaluate("0 == true"))
        assertEquals(false, evaluate("1 == false"))
        assertEquals(true, evaluate("1.0 == true"))
        assertEquals(true, evaluate("0.0 == false"))
        assertEquals(false, evaluate("2.5 == true"))
        assertEquals(false, evaluate("2.5 == false"))
        assertEquals(false, evaluate("2 == true"))
        assertEquals(false, evaluate("2 == false"))

        // Any string other than 'true' is coerced to false
        assertEquals(true, evaluate("false == ''"))
        assertEquals(true, evaluate("false == 'any string that is not a boolean'"))
        assertEquals(false, evaluate("true == ''"))
        assertEquals(false, evaluate("true == 'any string that is not a boolean'"))
    }

    @Test
    fun testDivideByZero() {
        assertEquals(false, evaluate("2 == 2 / 0"))
    }

    companion object {
        private fun evaluate(expression: String): Any? {
            return Expression(expression).evaluate()
        }
    }
}
