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
        assertEquals(0.0, evaluate("d2:zScoreWFH(60, 6, \"male\")"))
    }

    @Test
    fun testZScoreHFA_Female() {
        assertEquals(-1.0, evaluate("d2:zScoreWFH(50, 3.1, \"f\")"))
        assertEquals(-3.0, evaluate("d2:zScoreWFH(60.0, 4.5, \"female\")"))
    }

    @Test
    fun testZScoreWFH_LengthToHeightSwitchAt87() {
        // below 87 cm the WHO weight-for-length table applies, from 87 cm the weight-for-height table
        assertEquals(0.0, evaluate("d2:zScoreWFH(86.9, 12.0, \"m\")"))
        assertEquals(0.0, evaluate("d2:zScoreWFH(87.0, 12.2, \"m\")"))
        assertEquals(-0.2, evaluate("d2:zScoreWFH(87.0, 12.0, \"m\")") as Double, interpolationMargin)
        assertEquals(0.0, evaluate("d2:zScoreWFH(86.9, 11.7, \"f\")"))
        assertEquals(0.0, evaluate("d2:zScoreWFH(87.0, 11.9, \"f\")"))
        assertEquals(-0.2, evaluate("d2:zScoreWFH(87.0, 11.7, \"f\")") as Double, interpolationMargin)
    }

    @Test
    fun testZScoreWFH_HeightRange_Male() {
        assertEquals(0.0, evaluate("d2:zScoreWFH(90, 12.9, \"m\")"))
        assertEquals(-2.0, evaluate("d2:zScoreWFH(100, 13.1, \"m\")"))
        assertEquals(1.0, evaluate("d2:zScoreWFH(100, 16.7, \"m\")"))
        assertEquals(3.0, evaluate("d2:zScoreWFH(110, 24.4, \"m\")"))
        assertEquals(-1.0, evaluate("d2:zScoreWFH(110, 17.0, \"m\")"))
    }

    @Test
    fun testZScoreWFH_Female() {
        assertEquals(-2.11, evaluate("d2:zScoreWFH(95, 11.6, \"f\")"))
        assertEquals(-3.50, evaluate("d2:zScoreWFH(95, 10.7, \"f\")"))
        assertEquals(-2.71, evaluate("d2:zScoreWFH(112.4, 15.3, \"f\")"))
    }

    @Test
    fun testZScoreWFH_HeightRange_Female() {
        assertEquals(-2.0, evaluate("d2:zScoreWFH(90, 10.6, \"f\")"))
        assertEquals(0.0, evaluate("d2:zScoreWFH(100, 15.2, \"f\")"))
        assertEquals(2.0, evaluate("d2:zScoreWFH(100, 18.4, \"f\")"))
        assertEquals(-3.0, evaluate("d2:zScoreWFH(110, 14.2, \"f\")"))
        assertEquals(1.0, evaluate("d2:zScoreWFH(110, 20.5, \"f\")"))
    }

    @Test
    fun testZScoreWFH_TenthOfCentimeter() {
        assertEquals(0.0, evaluate("d2:zScoreWFH(64.9, 7.2, \"m\")"))
        assertEquals(-2.0, evaluate("d2:zScoreWFH(87.3, 10.5, \"m\")"))
        assertEquals(2.0, evaluate("d2:zScoreWFH(95.7, 16.8, \"m\")"))
        assertEquals(-1.0, evaluate("d2:zScoreWFH(104.2, 15.3, \"m\")"))
        assertEquals(0.0, evaluate("d2:zScoreWFH(64.9, 7.1, \"f\")"))
        assertEquals(1.0, evaluate("d2:zScoreWFH(87.3, 13.1, \"f\")"))
        assertEquals(-2.0, evaluate("d2:zScoreWFH(95.7, 11.8, \"f\")"))
        assertEquals(3.0, evaluate("d2:zScoreWFH(104.2, 22.1, \"f\")"))
    }

    @Test
    fun testZScoreWFH_Female112() {
        assertEquals(-3.0, evaluate("d2:zScoreWFH(112, 14.8, \"f\")"))
        assertEquals(0.0, evaluate("d2:zScoreWFH(112, 19.4, \"f\")"))
        assertEquals(0.3, evaluate("d2:zScoreWFH(112, 20.0, \"f\")") as Double, interpolationMargin)
        assertEquals(3.0, evaluate("d2:zScoreWFH(112, 26.2, \"f\")"))
    }

    @Test
    fun testZScoreWFH_TableBounds() {
        assertEquals(0.0, evaluate("d2:zScoreWFH(45, 2.4, \"m\")"))
        assertEquals(3.0, evaluate("d2:zScoreWFH(120, 30.1, \"m\")"))
        assertEquals(-3.0, evaluate("d2:zScoreWFH(120, 17.3, \"f\")"))
        assertEquals(3.5, evaluate("d2:zScoreWFH(100, 25, \"m\")"))
        assertEquals(-3.5, evaluate("d2:zScoreWFH(100, 10, \"f\")"))
    }

    @Test
    fun testZScoreWFH_UnknownHeight() {
        for (height in listOf("44.9", "87.25", "120.1")) {
            val ex = assertFailsWith(IllegalArgumentException::class) { evaluate("d2:zScoreWFH($height, 12, \"m\")") }
            assertEquals("No key exist for provided parameters", ex.message)
        }
    }

    // interpolated values are truncated (not rounded) to 2 decimals, so float noise can cost 0.01
    private val interpolationMargin: Double = 0.015

    private fun evaluate(expression: String): Any? {
        return Expression(expression, ExpressionMode.RULE_ENGINE_ACTION).evaluate()
    }
}