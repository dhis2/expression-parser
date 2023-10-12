package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.spi.IllegalExpressionException
import org.hisp.dhis.lib.expression.Expression.Mode

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Test of the `d2:ceil` function.
 *
 *
 * Translated from existing test of same name in rule-engine.
 *
 * @author Jan Bernitt
 */
internal class CeilTest {
    @Test
    fun evaluateMustReturnCeiledValue() {
        assertEquals(5.0, evaluate("d2:ceil(4.1)"))
        assertEquals(1.0, evaluate("d2:ceil(0.8)"))
        assertEquals(6.0, evaluate("d2:ceil(5.1)"))
        assertEquals(1.0, evaluate("d2:ceil(1)"))
        assertEquals(-9.0, evaluate("d2:ceil(-9.3)"))
        assertEquals(-5.0, evaluate("d2:ceil(-5.9)"))
    }

    @Test
    fun return_zero_when_number_is_invalid() {
        // ANTLR would return 0
        assertFailsWith(IllegalExpressionException::class) { evaluate("d2:ceil('str')") }
    }

    @Test
    fun return_NaN_when_input_is_NaN() {
        assertEquals(Double.NaN, evaluate("d2:ceil(1%0)"))
    }

    companion object {
        private fun evaluate(expression: String): Double {
            return Expression(expression, Mode.RULE_ENGINE_ACTION).evaluate() as Double
        }
    }
}
