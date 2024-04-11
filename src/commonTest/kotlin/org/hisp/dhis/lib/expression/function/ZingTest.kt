package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.ExpressionMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Test of the `d2:zing` function.
 *
 * ZING is short for Zero If NeGative
 *
 * @author Jan Bernitt
 */
internal class ZingTest {

    @Test
    fun testZing_Null() {
        assertNull(evaluate("d2:zing(null)"))
    }

    @Test
    fun testZing_Negative() {
        assertEquals(0.0, evaluate("d2:zing(-1)"))
        assertEquals(0.0, evaluate("d2:zing(-1.3)"))
        assertEquals(0.0, evaluate("d2:zing(-0.3)"))
    }

    @Test
    fun testZing_Zero() {
        assertEquals(0.0, evaluate("d2:zing(0)"))
        assertEquals(0.0, evaluate("d2:zing(0.0)"))
    }

    @Test
    fun testZing_Positive() {
        assertEquals(1.0, evaluate("d2:zing(1)"))
        assertEquals(1.42, evaluate("d2:zing(1.42)"))
    }

    private fun evaluate(expression: String): Any? {
        return Expression(expression, ExpressionMode.RULE_ENGINE_ACTION).evaluate()
    }
}