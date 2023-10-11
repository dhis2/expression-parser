package org.hisp.dhis.lib.expression

import org.hisp.dhis.lib.expression.spi.IllegalExpressionException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * Tests the [NamedFunction]s.
 *
 * @author Jan Bernitt
 */
internal class FunctionsExpressionTest {
    @Test
    fun testFirstNonNull_Numbers() {
        assertEquals(42.0, evaluate("firstNonNull(null, 42, 23)"))
    }

    @Test
    fun testFirstNonNull_Strings() {
        assertEquals("42", evaluate("firstNonNull(null,null, \"42\", \"23\")"))
    }

    @Test
    fun testFirstNonNull_Booleans() {
        assertEquals(true, evaluate("firstNonNull(true, null, false)"))
    }

    @Test
    fun testGreatest() {
        assertEquals(5.0, evaluate("greatest(3,2.2,5.0,0.4)"))
    }

    @Test
    fun testGreatest_Null() {
        assertEquals(5.0, evaluate("greatest(null,4,5.0)"))
    }

    @Test
    fun testGreatest_NaN() {
        assertEquals(Double.NaN, evaluate("greatest(1%0,4,5.0)"))
    }

    @Test
    fun testGreatest_PositiveInfinity() {
        assertEquals(Double.POSITIVE_INFINITY, evaluate("greatest(1/0,4,5.0)"))
    }

    @Test
    fun testGreatest_Empty() {
        assertNull(evaluate("greatest()"))
    }

    @Test
    fun testIf_True() {
        assertEquals(1.0, evaluate("if(true, 1, 0)"))
        assertEquals(true, evaluate("if(1 < 4, true, false)"))
        assertNull(evaluate("if(1, null, 42)"))
    }

    @Test
    fun testIf_False() {
        assertNull(evaluate("if(false, 1, null)"))
        assertEquals(false, evaluate("if(1 > 4, true, false)"))
        assertEquals(42.0, evaluate("if(0, null, 42)"))
    }

    @Test
    fun testIf_Null() {
        assertEquals(false, evaluate("if(null, true, false)"))
    }

    @Test
    fun testIf_NaN() {
        val ex = assertFailsWith(IllegalExpressionException::class) { evaluate("if(1%0, true, false)") }
        assertEquals(
            "Failed to coerce value 'NaN' (Double) to Boolean: Could not coerce Double 'NaN' to Boolean\n" +
                    "\t in expression: 1 % 0", ex.message)
    }

    @Test
    fun testIsNotNull() {
        assertEquals(true, evaluate("isNotNull(2)"))
        assertEquals(true, evaluate("isNotNull(42.0)"))
        assertEquals(true, evaluate("isNotNull('hello')"))
        assertEquals(true, evaluate("isNotNull(false)"))
        assertEquals(true, evaluate("isNotNull(1%0)"))
        assertEquals(true, evaluate("isNotNull(1/0)"))
        assertEquals(false, evaluate("isNotNull(null)"))
    }

    @Test
    fun testIsNull() {
        assertEquals(false, evaluate("isNull(2)"))
        assertEquals(false, evaluate("isNull(42.0)"))
        assertEquals(false, evaluate("isNull('hello')"))
        assertEquals(false, evaluate("isNull(false)"))
        assertEquals(false, evaluate("isNull(1%0)"))
        assertEquals(false, evaluate("isNull(1/0)"))
        assertEquals(true, evaluate("isNull(null)"))
    }

    @Test
    fun testLeast() {
        assertEquals(0.4, evaluate("least(3,2.2,5.0,0.4)"))
    }

    @Test
    fun testLeast_Null() {
        assertEquals(4.0, evaluate("least(null,4,5.0)"))
    }

    @Test
    fun testLeast_NaN() {
        assertEquals(4.0, evaluate("least(1%0,4,5.0)"))
    }

    @Test
    fun testLeast_PositiveInfinity() {
        assertEquals(Double.NEGATIVE_INFINITY, evaluate("least(-1/0,4,5.0,1/0)"))
    }

    @Test
    fun testLeast_Empty() {
        assertNull(evaluate("least()"))
    }

    @Test
    fun testRemoveZeros() {
        assertNull(evaluate("removeZeros(0.0)"))
        assertNull(evaluate("removeZeros(0)"))
        assertEquals(42.0, evaluate("removeZeros(42.0)"))
    }

    @Test
    fun testBlockWhitespaceSingleArgs() {
        assertEquals(false, evaluate("isNull(2)"))
        assertEquals(false, evaluate("isNull( 2)"))
        assertEquals(false, evaluate("isNull(2 )"))
        assertEquals(false, evaluate("isNull( 2 )"))
    }

    @Test
    fun testBlockWhitespaceMultiArgs() {
        assertEquals(Double.POSITIVE_INFINITY, evaluate("log(2,1)"))
        assertEquals(Double.POSITIVE_INFINITY, evaluate("log( 2, 1)"))
        assertEquals(Double.POSITIVE_INFINITY, evaluate("log(2 , 1 )"))
        assertEquals(Double.POSITIVE_INFINITY, evaluate("log( 2 , 1 )"))
    }

    @Test
    fun testBlockWhitespaceVaragrs() {
        assertNull(evaluate("firstNonNull()"))
        assertEquals(2.0, evaluate("firstNonNull(2)"))
        assertEquals(2.0, evaluate("firstNonNull( 2)"))
        assertEquals(2.0, evaluate("firstNonNull(2 )"))
        assertEquals(2.0, evaluate("firstNonNull( 2 )"))
        assertEquals(2.0, evaluate("firstNonNull(2,3)"))
        assertEquals(2.0, evaluate("firstNonNull( 2, 3)"))
        assertEquals(2.0, evaluate("firstNonNull(2 , 3 )"))
        assertEquals(2.0, evaluate("firstNonNull( 2 , 3 )"))
    }

    companion object {
        private fun evaluate(expression: String): Any? {
            return Expression(expression).evaluate()
        }
    }
}
