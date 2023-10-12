package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests the `least` function
 *
 * @author Jan Bernitt
 */
internal class LeastTest {

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

    private fun evaluate(expression: String): Any? {
        return Expression(expression).evaluate()
    }
}