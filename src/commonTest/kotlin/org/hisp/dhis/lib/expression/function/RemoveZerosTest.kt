package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests the `removeZeros` function
 *
 * @author Jan Bernitt
 */
internal class RemoveZerosTest {

    @Test
    fun testRemoveZeros() {
        assertNull(evaluate("removeZeros(0.0)"))
        assertNull(evaluate("removeZeros(0)"))
        assertEquals(42.0, evaluate("removeZeros(42.0)"))
    }

    private fun evaluate(expression: String): Any? {
        return Expression(expression).evaluate()
    }
}