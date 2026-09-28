package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.ExpressionMode
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
        assertEquals(-1.01, evaluate("d2:zScoreHFA(30, 88.5, \"m\")"))
        assertEquals(1.0, evaluate("d2:zScoreHFA(20, 87, \"male\")"))
    }

    @Test
    fun testZScoreHFA_Female() {
        assertEquals(-2.02, evaluate("d2:zScoreHFA(10, 66.5, \"f\")"))
        assertEquals(-3.0, evaluate("d2:zScoreHFA(30.0, 80.1, \"female\")"))
    }

    @Test
    fun testZScoreHFA_LengthToHeightSwitchAt24Months() {
        // below 24 months the WHO length-for-age table applies, from 24 months the height-for-age table
        assertEquals(0.15, evaluate("d2:zScoreHFA(23, 86.0, \"f\")"))
        assertEquals(0.09, evaluate("d2:zScoreHFA(24, 86.0, \"f\")"))
        assertEquals(-0.31, evaluate("d2:zScoreHFA(23, 86.0, \"m\")"))
        assertEquals(-0.37, evaluate("d2:zScoreHFA(24, 86.0, \"m\")"))
    }

    @Test
    fun testZScoreHFA_TableBounds() {
        assertEquals(0.01, evaluate("d2:zScoreHFA(0, 49.9, \"m\")"))
        assertEquals(0.0, evaluate("d2:zScoreHFA(60, 109.4, \"f\")"))
    }

    @Test
    fun testZScoreHFA_BeyondThreeSD() {
        assertEquals(-4.52, evaluate("d2:zScoreHFA(12, 65.0, \"m\")"))
        assertEquals(4.27, evaluate("d2:zScoreHFA(12, 85.0, \"f\")"))
    }

    private fun evaluate(expression: String): Any? {
        return Expression(expression, ExpressionMode.RULE_ENGINE_ACTION).evaluate()
    }
}