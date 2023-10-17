package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.spi.ValueType
import org.hisp.dhis.lib.expression.util.RuleVariableValue
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test of the `d2:count` function.
 *
 * @author Jan Bernitt
 */
internal class CountCandidatesTest : AbstractVariableBasedTest() {

    @Test
    fun testCount_NoCandidates() {
        val values = mapOf("v1" to RuleVariableValue(ValueType.STRING).copy(value = "value"))
        assertEquals(0, evaluate("d2:count(#{v1})", values))
    }

    @Test
    fun testCount_EmptyCandidates() {
        val values = mapOf("v1" to RuleVariableValue(ValueType.STRING).copy(candidates = listOf()))
        assertEquals(0, evaluate("d2:count(#{v1})", values))
    }

    @Test
    fun testCount_Candidates() {
        val values = mapOf("v1" to RuleVariableValue(ValueType.NUMBER).copy(candidates = listOf("1")))
        assertEquals(1, evaluate("d2:count(#{v1})", values))

        val values2 = mapOf("v1" to RuleVariableValue(ValueType.NUMBER).copy(candidates = listOf("1", "2")))
        assertEquals(2, evaluate("d2:count(#{v1})", values2))
    }
}