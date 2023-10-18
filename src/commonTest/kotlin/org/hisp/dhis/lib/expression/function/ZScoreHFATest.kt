package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Test of the `d2:zScoreHFA` function.
 *
 * @author Jan Bernitt
 */
internal class ZScoreHFATest {

    @Test
    fun testZScoreHFA_Null() {
        val ex = assertFailsWith(IllegalArgumentException::class) {evaluate("d2:zScoreHFA(30, 88.5, null)")}
        assertEquals("Gender cannot be null", ex.message)
        val ex2 = assertFailsWith(IllegalArgumentException::class) {evaluate("d2:zScoreHFA(30, null, \"m\")")}
        assertEquals("Weight cannot be null", ex2.message)
        val ex3 = assertFailsWith(IllegalArgumentException::class) {evaluate("d2:zScoreHFA(null, 88.5, \"m\")")}
        assertEquals("Parameter cannot be null", ex3.message)
    }

    @Test
    fun testZScoreHFA_Male() {
        assertEquals(-1.0, evaluate("d2:zScoreHFA(30, 88.5, \"m\")"))
        assertEquals(1.0, evaluate("d2:zScoreHFA(20, 87, \"male\")"))
    }

    @Test
    fun testZScoreHFA_Female() {
        assertEquals(-2.0, evaluate("d2:zScoreHFA(10, 66.5, \"f\")"))
        assertEquals(-3.0, evaluate("d2:zScoreHFA(30.0, 80.1, \"female\")"))
    }

    private fun evaluate(expression: String): Any? {
        return Expression(expression, Expression.Mode.RULE_ENGINE_ACTION).evaluate()
    }
}