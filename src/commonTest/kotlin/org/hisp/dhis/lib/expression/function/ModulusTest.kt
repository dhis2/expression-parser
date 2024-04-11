package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.ExpressionMode
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test of the `d2:modulus` function.
 *
 * @author Jan Bernitt
 */
internal class ModulusTest {

    @Test
    fun testModulus_Zero() {
        assertEquals(Double.NaN, evaluate("d2:modulus(1,0)"))
        assertEquals(0.0, evaluate("d2:modulus(0,1)"))
        assertEquals(0.0, evaluate("d2:modulus(0,10)"))
    }

    @Test
    fun testModulus_Positive() {
        assertEquals(0.0, evaluate("d2:modulus(10,5)"))
        assertEquals(2.0, evaluate("d2:modulus(2,5)"))
        assertEquals(1.0, evaluate("d2:modulus(5,2)"))
    }

    @Test
    fun testModulus_Negative() {
        assertEquals(0.0, evaluate("d2:modulus(-10,5)"))
        assertEquals(0.0, evaluate("d2:modulus(10,-5)"))
        assertEquals(2.0, evaluate("d2:modulus(2,-5)"))
        assertEquals(-3.0, evaluate("d2:modulus(7,-5)"))
    }

    private fun evaluate(expression: String): Double? {
        return Expression(expression, ExpressionMode.RULE_ENGINE_ACTION).evaluate() as Double?
    }
}