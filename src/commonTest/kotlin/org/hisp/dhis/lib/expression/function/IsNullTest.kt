package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the `isNull` function
 *
 * @author Jan Bernitt
 */
internal class IsNullTest {

    @Test
    fun testIsNull() {
        assertEquals(true, evaluate("isNull(null)"))
    }

    @Test
    fun testIsNull_EmptyString() {
        assertEquals(false, evaluate("isNull(\"\")"))
    }

    @Test
    fun testIsNull_Whitespace() {
        assertEquals(false, evaluate("isNull(2)"))
        assertEquals(false, evaluate("isNull( 2)"))
        assertEquals(false, evaluate("isNull(2 )"))
        assertEquals(false, evaluate("isNull( 2 )"))
    }

    private fun evaluate(expression: String): Any? {
        return Expression(expression).evaluate()
    }
}