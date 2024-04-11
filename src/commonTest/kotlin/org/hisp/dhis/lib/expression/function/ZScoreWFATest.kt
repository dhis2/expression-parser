package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.ExpressionMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Test of the `d2:zScoreWFA` function.
 *
 * @author Jan Bernitt
 */
internal class ZScoreWFATest {

    @Test
    fun testZScoreHFA_Null() {
        val ex = assertFailsWith(IllegalArgumentException::class) {evaluate("d2:zScoreWFA(30, 88.5, null)")}
        assertEquals("Gender cannot be null", ex.message)
        val ex2 = assertFailsWith(IllegalArgumentException::class) {evaluate("d2:zScoreWFA(30, null, \"m\")")}
        assertEquals("Weight cannot be null", ex2.message)
        val ex3 = assertFailsWith(IllegalArgumentException::class) {evaluate("d2:zScoreWFA(null, 88.5, \"m\")")}
        assertEquals("Parameter cannot be null", ex3.message)
    }

    @Test
    fun testZScoreHFA_Male() {
        assertEquals(-3.0, evaluate("d2:zScoreWFA(30, 9.4, \"m\")"))
        assertEquals(1.0, evaluate("d2:zScoreWFA(20, 12.7, \"male\")"))
    }

    @Test
    fun testZScoreHFA_Female() {
        assertEquals(-2.0, evaluate("d2:zScoreWFA(10, 6.7, \"f\")"))
        assertEquals(3.0, evaluate("d2:zScoreWFA(30.0, 19.0, \"female\")"))
    }

    private fun evaluate(expression: String): Any? {
        return Expression(expression, ExpressionMode.RULE_ENGINE_ACTION).evaluate()
    }
}