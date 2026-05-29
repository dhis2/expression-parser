package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.ExpressionMode
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the `pow` function
 *
 * @author Jan Bernitt / Tony Valle
 */
internal class PowTest {

    @Test
    fun testPow() {
        assertEquals(1.0, evaluate("d2:pow(49, 0)"))
        assertEquals(7.0, evaluate("d2:pow(49, 0.5)"))
        assertEquals(49.0, evaluate("d2:pow(49, 1)"))
    }

    @Test
    fun testLog_Whitespace() {
        assertEquals(2.0, evaluate("d2:pow(2,1)"))
        assertEquals(2.0, evaluate("d2:pow( 2, 1)"))
        assertEquals(2.0, evaluate("d2:pow(2 , 1 )"))
        assertEquals(2.0, evaluate("d2:pow( 2 , 1 )"))
    }

    private fun evaluate(expression: String): Any? {
        return Expression(expression, ExpressionMode.RULE_ENGINE_ACTION).evaluate()
    }
}
