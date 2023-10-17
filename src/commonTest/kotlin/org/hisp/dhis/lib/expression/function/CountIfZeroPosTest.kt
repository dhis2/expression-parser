package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.spi.ValueType
import org.hisp.dhis.lib.expression.util.RuleVariableValue
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test of the `d2:countIfZeroPos` function.
 *
 * @author Jan Bernitt
 */
internal class CountIfZeroPosTest : AbstractVariableBasedTest() {

    @Test
    fun testCountIfZeroPos_Empty() {
        val values = mapOf("v1" to RuleVariableValue(ValueType.NUMBER).copy(candidates = listOf()))
        assertEquals(0, evaluate("d2:countIfZeroPos(#{v1})", values))
    }

    @Test
    fun testCountIfZeroPos_Zero() {
        val values = mapOf("v1" to RuleVariableValue(ValueType.NUMBER).copy(candidates = listOf("0", "0.0")))
        assertEquals(2, evaluate("d2:countIfZeroPos(#{v1})", values))
    }

    @Test
    fun testCountIfZeroPos_Positive() {
        val values = mapOf("v1" to RuleVariableValue(ValueType.NUMBER).copy(candidates = listOf("1", "2", "0.0")))
        assertEquals(3, evaluate("d2:countIfZeroPos(#{v1})", values))
    }

    @Test
    fun testCountIfZeroPos_Negative() {
        val values = mapOf("v1" to RuleVariableValue(ValueType.NUMBER).copy(candidates = listOf("-2", "-1", "-0.2")))
        assertEquals(0, evaluate("d2:countIfZeroPos(#{v1})", values))
    }

    @Test
    fun testCountIfZeroPos_Mixed() {
        val values = mapOf("v1" to RuleVariableValue(ValueType.NUMBER).copy(candidates = listOf("1", "2", "-1", "0.2", "0.0")))
        assertEquals(4, evaluate("d2:countIfZeroPos(#{v1})", values))
    }
}