package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests the `firstNonNull` function
 *
 * @author Jan Bernitt
 */
internal class FirstNonNullTest {

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
    fun testFirstNonNull_Whitespace() {
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

    private fun evaluate(expression: String): Any? {
        return Expression(expression).evaluate()
    }
}