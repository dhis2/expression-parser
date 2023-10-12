package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.spi.IllegalExpressionException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * Tests the `if` function
 *
 * @author Jan Bernitt
 */
class IfTest {

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
            "Failed to coerce value 'NaN' (Double) to Boolean in expression: 1 % 0", ex.message)
    }

    private fun evaluate(expression: String): Any? {
        return Expression(expression).evaluate()
    }
}