package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.spi.ValueType
import org.hisp.dhis.lib.expression.spi.VariableValue
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test of the `d2:maxValue` function.
 *
 * @author Jan Bernitt
 */
internal class MaxValueTest : AbstractVariableBasedTest() {

    @Test
    fun testMaxValue_Empty() {
        val values = mapOf("v1" to VariableValue(ValueType.NUMBER).copy(candidates = listOf()))
        assertEquals(Double.NaN, evaluate("d2:maxValue(#{v1})", values))
    }

    @Test
    fun testMaxValue() {
        val values = mapOf("v1" to VariableValue(ValueType.NUMBER).copy(candidates = listOf("1", "42", "-100")))
        assertEquals(42.0, evaluate("d2:maxValue(#{v1})", values))
    }
}