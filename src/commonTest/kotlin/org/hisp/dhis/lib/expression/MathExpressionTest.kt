package org.hisp.dhis.lib.expression

import kotlin.test.Test

/**
 * Port of the ANTLR `MathExpressionTest` and some more.
 */
internal class MathExpressionTest {
    @Test
    fun testSum() {
        assertEquals(2.0, evaluate("1 + 1"))
        assertEquals(2.3, evaluate("1.3 + 1"))
        assertEquals(3.3, evaluate("1.3 + ( 1 + 1 )"))
    }

    @Test
    fun testSub() {
        assertEquals(0.0, evaluate("+ 1 - 1"))
        assertEquals(0.3, evaluate("1.3 - 1"))
        assertEquals(-0.7, evaluate("1.3 - ( 1 + 1 )"))
    }

    @Test
    fun testDivide() {
        assertEquals(3.333333333333333, evaluate("10/3"))
        assertEquals(1.0, evaluate("1/1"))
        assertEquals(0.5, evaluate("1/2"))
        assertEquals(-0.7, evaluate("-1.4/( 1 + 1 )"))
        assertEquals(1.0, evaluate("( 1 / 1000 ) * 1000"))
        assertEquals(Double.POSITIVE_INFINITY, evaluate("1.0/( 1 - 1 )"))
        assertEquals(Double.POSITIVE_INFINITY, evaluate("80 + 4 / 0"))
    }

    @Test
    fun testMultiply() {
        assertEquals(1.0, evaluate("1*1"))
        assertEquals(2.0, evaluate("1*2"))
        assertEquals(-2.8, evaluate("-1.4*( 1 + 1 )"))
    }

    @Test
    fun testLog() {
        assertEquals(4.605170, evaluate("log(100)"))
        assertEquals(-0.693147, evaluate("log( .5 )"))
        assertEquals(3.0, evaluate("log(8,2)"))
        assertEquals(2.0, evaluate("log(256, 16)"))
        assertEquals(2.0, evaluate("log( 100, 10 )"))
    }

    @Test
    fun testLog10() {
        assertEquals(2.0, evaluate("log10(100)"))
        assertEquals(-0.301030, evaluate("log10( .5 )"))
    }

    @Test
    fun testPower() {
        assertEquals(1.0, evaluate("1^10"))
        assertEquals(25.0, evaluate("5^2"))
        assertEquals(1.96, evaluate("1.4^( 1 + 1 )"))
        assertEquals(1.0, evaluate("1.4^( 1 - 1 )"))
        assertEquals(4.0, evaluate("2^2"))
        assertEquals(0.25, evaluate("2^-2"))
        assertEquals(4.0, evaluate("2^--2"))
        assertEquals(16.0, evaluate("2^2^2"))
        assertEquals(9.0, evaluate("(-3)^2"))
        assertEquals(-27.0, evaluate("(-3)^3"))
        assertEquals(-27.0, evaluate("-3^3"))
        assertEquals(-9.0, evaluate("-3^2"))
    }

    @Test
    fun testModulo() {
        assertEquals(1.0, evaluate("1%2"))
        assertEquals(1.0, evaluate("5%2"))
        assertEquals(1.4, evaluate("1.4%( 1 + 1 )"))
        assertEquals(Double.NaN, evaluate("1.0%( 1 - 1 )"))
    }

    @Test
    fun testBracketsAndOperatorPrecedence() {
        assertEquals(-2.25141952945498701E18, evaluate("(1+2)*-3^(9*4)*5+6"))
        assertEquals(-787313.0, evaluate("1+2*-3^9*4*5+6"))
    }
    private fun assertEquals(expected: Double, actual: Number) {
        kotlin.test.assertEquals(expected, actual.toDouble(), 0.000001)
    }
    private fun evaluate(expression: String): Number {
        val expr = Expression(expression)
        val clean: (String) -> String = { str: String -> str.replace("\\s+".toRegex(), "") }
        kotlin.test.assertEquals(clean(expression), clean(expr.normalise()))
        return expr.evaluate() as Number
    }

}
