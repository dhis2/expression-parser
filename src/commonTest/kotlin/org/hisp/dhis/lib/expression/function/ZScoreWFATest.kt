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
        assertEquals(-2.97, evaluate("d2:zScoreWFA(30, 9.4, \"m\")"))
        assertEquals(1.01, evaluate("d2:zScoreWFA(20, 12.7, \"male\")"))
    }

    @Test
    fun testZScoreHFA_Female() {
        assertEquals(-1.97, evaluate("d2:zScoreWFA(10, 6.7, \"f\")"))
        assertEquals(3.01, evaluate("d2:zScoreWFA(30.0, 19.0, \"female\")"))
    }

    @Test
    fun testZScoreWFA_Month28Male() {
        assertEquals(-0.02, evaluate("d2:zScoreWFA(28, 12.9, \"m\")"))
        assertEquals(-2.21, evaluate("d2:zScoreWFA(28, 10.0, \"m\")"))
    }

    @Test
    fun testZScoreWFA_TableBounds() {
        assertEquals(-0.1, evaluate("d2:zScoreWFA(0, 3.3, \"m\")"))
        assertEquals(-0.01, evaluate("d2:zScoreWFA(60, 18.2, \"f\")"))
    }

    @Test
    fun testZScoreWFA_BeyondThreeSD() {
        // WHO restricted application: beyond +/-3 SD the z-score continues linearly
        assertEquals(-5.37, evaluate("d2:zScoreWFA(12, 5.0, \"m\")"))
        assertEquals(4.79, evaluate("d2:zScoreWFA(12, 16.0, \"f\")"))
    }

    @Test
    fun testZScoreWFA_FiveToTenYears() {
        // WHO growth reference 2007
        assertEquals(0.0, evaluate("d2:zScoreWFA(61, 18.5, \"m\")"))
        assertEquals(0.02, evaluate("d2:zScoreWFA(61, 18.3, \"f\")"))
        assertEquals(0.25, evaluate("d2:zScoreWFA(90, 25.0, \"m\")"))
        assertEquals(-1.1, evaluate("d2:zScoreWFA(90, 20.0, \"f\")"))
        assertEquals(0.01, evaluate("d2:zScoreWFA(120, 31.2, \"m\")"))
        assertEquals(1.23, evaluate("d2:zScoreWFA(120, 40.0, \"f\")"))
        assertEquals(-4.22, evaluate("d2:zScoreWFA(100, 15.0, \"m\")"))
        assertEquals(3.41, evaluate("d2:zScoreWFA(100, 50.0, \"f\")"))
    }

    @Test
    fun testZScoreWFA_UnknownAge() {
        for (age in listOf("-1", "6.5", "121")) {
            val ex = assertFailsWith(IllegalArgumentException::class) { evaluate("d2:zScoreWFA($age, 8, \"m\")") }
            assertEquals("No key exist for provided parameters", ex.message)
        }
    }

    private fun evaluate(expression: String): Any? {
        return Expression(expression, ExpressionMode.RULE_ENGINE_ACTION).evaluate()
    }
}