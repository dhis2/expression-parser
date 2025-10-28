package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.ExpressionMode
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the `d2:condition` function
 *
 * @author Jan Bernitt
 */
class ConditionTest {

    @Test
    fun testD2_Condition() {
        assertEquals(1.0, evaluate("d2:condition('true', 1, 0)"))
        assertEquals(true, evaluate("d2:condition('42 < 100', true, false)"))
        assertEquals("yes", evaluate("d2:condition('if(true, true, false)', 'yes', 'no')"))
    }

    private fun evaluate(expression: String): Any? {
        return Expression(expression, ExpressionMode.ANDROID_CUSTOM_INTENT_EXPRESSION).evaluate()
    }
}