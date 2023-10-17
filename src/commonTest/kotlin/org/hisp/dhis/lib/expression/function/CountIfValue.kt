package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.spi.ValueType
import org.hisp.dhis.lib.expression.util.RuleVariableValue
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test of the `d2:countIfValue` function.
 *
 * @author Jan Bernitt
 */
internal class CountIfValue : AbstractVariableBasedTest() {

    @Test
    fun testCountIfValue_Null() {
        val values = mapOf("v1" to RuleVariableValue(ValueType.BOOLEAN).copy(candidates = listOf("true")))
        assertEquals(0, evaluate("d2:countIfValue(#{v1}, null)", values))
    }

    @Test
    fun testCountIfValue_NoCandidates() {
        val values = mapOf("v1" to RuleVariableValue(ValueType.BOOLEAN).copy(candidates = listOf()))
        assertEquals(0, evaluate("d2:countIfValue(#{v1}, true)", values))
    }

    @Test
    fun testCountIfValue_Number() {
        val values = mapOf("v1" to RuleVariableValue(ValueType.NUMBER).copy(candidates = listOf("1", "2", "1")))
        assertEquals(2, evaluate("d2:countIfValue(#{v1}, 1)", values))
        assertEquals(1, evaluate("d2:countIfValue(#{v1}, 2)", values))
        assertEquals(0, evaluate("d2:countIfValue(#{v1}, 3)", values))
    }

    @Test
    fun testCountIfValue_Boolean() {
        val values = mapOf("v1" to RuleVariableValue(ValueType.BOOLEAN).copy(candidates = listOf("true", "false", "true")))
        assertEquals(2, evaluate("d2:countIfValue(#{v1}, true)", values))
        assertEquals(1, evaluate("d2:countIfValue(#{v1}, false)", values))
    }

    @Test
    fun testCountIfValue_String() {
        val values = mapOf("v1" to RuleVariableValue(ValueType.STRING).copy(candidates = listOf("a", "b", "c", "a")))
        assertEquals(2, evaluate("d2:countIfValue(#{v1}, \"a\")", values))
        assertEquals(1, evaluate("d2:countIfValue(#{v1}, \"b\")", values))
        assertEquals(0, evaluate("d2:countIfValue(#{v1}, \"d\")", values))
    }
}