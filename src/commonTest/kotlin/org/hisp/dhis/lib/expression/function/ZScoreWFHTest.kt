package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.ExpressionMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Test of the `d2:zScoreWFH` function.
 *
 * @author Jan Bernitt
 */
internal class ZScoreWFHTest {

    @Test
    fun testZScoreHFA_Null() {
        val ex = assertFailsWith(IllegalArgumentException::class) {evaluate("d2:zScoreWFH(30, 88.5, null)")}
        assertEquals("Gender cannot be null", ex.message)
        val ex2 = assertFailsWith(IllegalArgumentException::class) {evaluate("d2:zScoreWFH(30, null, \"m\")")}
        assertEquals("Weight cannot be null", ex2.message)
        val ex3 = assertFailsWith(IllegalArgumentException::class) {evaluate("d2:zScoreWFH(null, 88.5, \"m\")")}
        assertEquals("Parameter cannot be null", ex3.message)
    }

    @Test
    fun testZScoreHFA_Male() {
        assertEquals(-2.0, evaluate("d2:zScoreWFH(50, 2.8, \"m\")"))
        assertEquals(0.02, evaluate("d2:zScoreWFH(60, 6, \"male\")"))
    }

    @Test
    fun testZScoreHFA_Female() {
        assertEquals(-0.87, evaluate("d2:zScoreWFH(50, 3.1, \"f\")"))
        assertEquals(-3.07, evaluate("d2:zScoreWFH(60.0, 4.5, \"female\")"))
    }

    @Test
    fun testZScoreWFH_LengthToHeightSwitchAt87() {
        // below 87 cm the WHO weight-for-length table applies, from 87 cm the weight-for-height table
        assertEquals(0.03, evaluate("d2:zScoreWFH(86.9, 12.0, \"m\")"))
        assertEquals(-0.17, evaluate("d2:zScoreWFH(87.0, 12.0, \"m\")"))
        assertEquals(0.1, evaluate("d2:zScoreWFH(86.9, 11.8, \"f\")"))
        assertEquals(-0.19, evaluate("d2:zScoreWFH(87.0, 11.7, \"f\")"))
    }

    @Test
    fun testZScoreWFH_HeightRange_Male() {
        assertEquals(0.01, evaluate("d2:zScoreWFH(90, 12.9, \"m\")"))
        assertEquals(-1.99, evaluate("d2:zScoreWFH(100, 13.1, \"m\")"))
        assertEquals(1.0, evaluate("d2:zScoreWFH(100, 16.7, \"m\")"))
        assertEquals(2.99, evaluate("d2:zScoreWFH(110, 24.4, \"m\")"))
        assertEquals(-0.99, evaluate("d2:zScoreWFH(110, 17.0, \"m\")"))
    }

    @Test
    fun testZScoreWFH_HeightRange_Female() {
        assertEquals(-2.05, evaluate("d2:zScoreWFH(90, 10.6, \"f\")"))
        assertEquals(-0.02, evaluate("d2:zScoreWFH(100, 15.2, \"f\")"))
        assertEquals(2.01, evaluate("d2:zScoreWFH(100, 18.4, \"f\")"))
        assertEquals(-3.02, evaluate("d2:zScoreWFH(110, 14.2, \"f\")"))
        assertEquals(1.01, evaluate("d2:zScoreWFH(110, 20.5, \"f\")"))
    }

    @Test
    fun testZScoreWFH_Female() {
        assertEquals(-2.08, evaluate("d2:zScoreWFH(95, 11.6, \"f\")"))
        assertEquals(-3.06, evaluate("d2:zScoreWFH(95, 10.7, \"f\")"))
        assertEquals(-2.70, evaluate("d2:zScoreWFH(112.37, 15.3, \"f\")"))
    }

    @Test
    fun testZScoreWFH_TenthOfCentimeter() {
        assertEquals(-0.07, evaluate("d2:zScoreWFH(64.9, 7.2, \"m\")"))
        assertEquals(-1.95, evaluate("d2:zScoreWFH(87.3, 10.5, \"m\")"))
        assertEquals(2.0, evaluate("d2:zScoreWFH(95.7, 16.8, \"m\")"))
        assertEquals(-0.98, evaluate("d2:zScoreWFH(104.2, 15.3, \"m\")"))
        assertEquals(0.06, evaluate("d2:zScoreWFH(64.9, 7.1, \"f\")"))
        assertEquals(0.99, evaluate("d2:zScoreWFH(87.3, 13.1, \"f\")"))
        assertEquals(-2.02, evaluate("d2:zScoreWFH(95.7, 11.8, \"f\")"))
        assertEquals(2.99, evaluate("d2:zScoreWFH(104.2, 22.1, \"f\")"))
    }

    @Test
    fun testZScoreWFH_Female112() {
        assertEquals(-3.0, evaluate("d2:zScoreWFH(112, 14.8, \"f\")"))
        assertEquals(0.0, evaluate("d2:zScoreWFH(112, 19.4, \"f\")"))
        assertEquals(0.32, evaluate("d2:zScoreWFH(112, 20.0, \"f\")"))
        assertEquals(2.98, evaluate("d2:zScoreWFH(112, 26.2, \"f\")"))
    }

    @Test
    fun testZScoreWFH_TableBounds() {
        assertEquals(-0.19, evaluate("d2:zScoreWFH(45, 2.4, \"m\")"))
        assertEquals(3.0, evaluate("d2:zScoreWFH(120, 30.1, \"m\")"))
        assertEquals(-2.97, evaluate("d2:zScoreWFH(120, 17.3, \"f\")"))
    }

    @Test
    fun testZScoreWFH_BeyondThreeSD() {
        // WHO restricted application: beyond +/-3 SD the z-score continues linearly
        assertEquals(6.03, evaluate("d2:zScoreWFH(100, 25, \"m\")"))
        assertEquals(-4.71, evaluate("d2:zScoreWFH(100, 10, \"f\")"))
        assertEquals(-5.44, evaluate("d2:zScoreWFH(45, 1.5, \"f\")"))
        assertEquals(4.18, evaluate("d2:zScoreWFH(120, 35, \"f\")"))
    }

    @Test
    fun testZScoreWFH_MatchesWhoAnthro() {
        // children over 2 years, measured standing
        assertEquals(-3.06, evaluate("d2:zScoreWFH(94.5, 11, \"m\")"))
        assertEquals(-2.96, evaluate("d2:zScoreWFH(94, 11, \"m\")"))
        assertEquals(-2.61, evaluate("d2:zScoreWFH(94.5, 11, \"f\")"))
        assertEquals(-2.5, evaluate("d2:zScoreWFH(94, 11, \"f\")"))
        // infants entered in Anthro as measured standing, which adds 0.7 cm (59, 54.5 and 66 cm)
        assertEquals(-3.39, evaluate("d2:zScoreWFH(59.7, 4.5, \"m\")"))
        assertEquals(-2.91, evaluate("d2:zScoreWFH(59.7, 4.5, \"f\")"))
        assertEquals(-2.99, evaluate("d2:zScoreWFH(55.2, 3.6, \"m\")"))
        assertEquals(-2.83, evaluate("d2:zScoreWFH(55.2, 3.6, \"f\")"))
        assertEquals(-3.11, evaluate("d2:zScoreWFH(66.7, 6, \"m\")"))
        assertEquals(-2.5, evaluate("d2:zScoreWFH(66.7, 6, \"f\")"))
    }

    @Test
    fun testZScoreWFH_BetweenTenthsOfCentimeter() {
        // L, M and S are interpolated between the neighbouring 0.1 cm rows, like WHO Anthro does
        assertEquals(-0.23, evaluate("d2:zScoreWFH(87.25, 12.0, \"m\")"))
        assertEquals(-0.25, evaluate("d2:zScoreWFH(87.25, 11.7, \"f\")"))
        assertEquals(-0.09, evaluate("d2:zScoreWFH(64.95, 7.2, \"m\")"))
        assertEquals(-0.03, evaluate("d2:zScoreWFH(100.04, 15.2, \"f\")"))
        assertEquals(0.02, evaluate("d2:zScoreWFH(119.99, 22.4, \"m\")"))
        // just below 87 cm the weight-for-length standard still applies
        assertEquals(0.02, evaluate("d2:zScoreWFH(86.95, 12.0, \"m\")"))
        assertEquals(0.09, evaluate("d2:zScoreWFH(86.95, 11.8, \"f\")"))
    }

    @Test
    fun testZScoreWFH_UnknownHeight() {
        for (height in listOf("44.9", "44.95", "120.05", "120.1")) {
            val ex = assertFailsWith(IllegalArgumentException::class) { evaluate("d2:zScoreWFH($height, 12, \"m\")") }
            assertEquals("No key exist for provided parameters", ex.message)
        }
    }

    private fun evaluate(expression: String): Any? {
        return Expression(expression, ExpressionMode.RULE_ENGINE_ACTION).evaluate()
    }
}
