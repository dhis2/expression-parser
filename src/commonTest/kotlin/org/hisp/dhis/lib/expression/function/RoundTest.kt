package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.ExpressionMode
import org.hisp.dhis.lib.expression.spi.ParseException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Test of the `d2:round` function.
 *
 * @author Jan Bernitt
 */
internal class RoundTest {

    @Test
    fun testRound_None() {
        val ex = assertFailsWith(ParseException::class) { evaluate("d2:round()")  }
        assertEquals("""Expected more arguments: ?,INTEGER
	at line:1 character:9
	d2:round()
	         ^""", ex.message)
    }

    @Test
    fun testRound_Null() {
        assertEquals(Double.NaN, evaluate("d2:round(null)") )
    }

    @Test
    fun testRound_NaN() {
        assertEquals(Double.NaN, evaluate("d2:round(1%0)") )
    }

    @Test
    fun testRound_Infinity() {
        assertEquals(Double.NaN, evaluate("d2:round(1/0)") )
    }

    @Test
    fun testRound_Zero() {
        assertEquals(0.0, evaluate("d2:round(0)") )
        assertEquals(0.0, evaluate("d2:round(+0)") )
        assertEquals(0.0, evaluate("d2:round(-0)") )
        assertEquals(0.0, evaluate("d2:round(0.0)") )
        assertEquals(0.0, evaluate("d2:round(+0.0)") )
        assertEquals(0.0, evaluate("d2:round(-0.0)") )
    }

    @Test
    fun testRound_Negative() {
        assertEquals(-1.0, evaluate("d2:round(-1.4)") )
        assertEquals(-2.0, evaluate("d2:round(-1.5)") )
    }

    @Test
    fun testRound_Positive() {
        assertEquals(1.0, evaluate("d2:round(1.4)") )
    }

    @Test
    fun testRound_WithPrecision() {
        assertEquals(1.40, evaluate("d2:round(1.4, 2)") )
        assertEquals(1.46, evaluate("d2:round(1.456789, 2)") )
        assertEquals(1.4568, evaluate("d2:round(1.456789, 4)") )
    }

    private fun evaluate(expression: String): Any? {
        return Expression(expression, ExpressionMode.RULE_ENGINE_ACTION).evaluate()
    }
}