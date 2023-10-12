package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests the `greatest` function
 *
 * @author Jan Bernitt
 */
internal class GreatestTest {

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

    private fun evaluate(expression: String): Any? {
        return Expression(expression).evaluate()
    }
}