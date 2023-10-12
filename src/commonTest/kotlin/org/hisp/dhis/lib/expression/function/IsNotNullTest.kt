package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the `isNotNull` function
 *
 * @author Jan Bernitt
 */
internal class IsNotNullTest {

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

    private fun evaluate(expression: String): Any? {
        return Expression(expression).evaluate()
    }
}