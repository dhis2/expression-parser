package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.spi.ValueType
import org.hisp.dhis.lib.expression.spi.VariableValue
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test of the `d2:minValue` function.
 *
 * @author Jan Bernitt
 */
internal class MinValueTest : AbstractVariableBasedTest() {

    @Test
    fun testMinValue_Empty() {
        val values = mapOf("v1" to VariableValue(ValueType.NUMBER).copy(candidates = listOf()))
        assertEquals(Double.NaN, evaluate("d2:minValue(#{v1})", values))
    }

    @Test
    fun testMinValue_Positive() {
        val values = mapOf("v1" to VariableValue(ValueType.NUMBER).copy(candidates = listOf("1", "42", "100")))
        assertEquals(1.0, evaluate("d2:minValue(#{v1})", values))
    }

    @Test
    fun testMinValue_Negative() {
        val values = mapOf("v1" to VariableValue(ValueType.NUMBER).copy(candidates = listOf("-1", "-42", "100")))
        assertEquals(-42.0, evaluate("d2:minValue(#{v1})", values))
    }
}