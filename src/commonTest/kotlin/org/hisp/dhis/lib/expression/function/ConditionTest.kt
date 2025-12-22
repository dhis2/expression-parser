package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.ExpressionMode
import org.hisp.dhis.lib.expression.spi.ValueType
import org.hisp.dhis.lib.expression.spi.VariableValue
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the `d2:condition` function
 *
 * @author Jan Bernitt
 */
class ConditionTest : AbstractVariableBasedTest() {

    @Test
    fun testD2_Condition() {
        assertCondition(1.0, "d2:condition('true', 1, 0)")
        assertCondition(true, "d2:condition('42 < 100', true, false)")
        assertCondition("yes", "d2:condition('if(true, true, false)', 'yes', 'no')")
        assertCondition("no", "d2:condition('d2:condition(\"true\", (\".\"), (\",\")) == (\",\")', 'yes', 'no')")
        assertCondition(1.0,"d2:condition('((1 == 1) && (2 == 2))', 1, 0)")
    }

    @Test
    fun expression_with_variables() {
        assertCondition("d2:condition('#{xxx1} * 100/ #{xxx2} >= 80', 100, 0)", mapOf(
            "xxx1" to VariableValue(ValueType.NUMBER).copy(value = "10"),
            "xxx2" to VariableValue(ValueType.NUMBER).copy(value = "13")), 0.0)
        assertCondition("d2:condition('#{xxx1} * 100/ #{xxx2} >= 80', 100, 0)", mapOf(
            "xxx1" to VariableValue(ValueType.NUMBER).copy(value = "11"),
            "xxx2" to VariableValue(ValueType.NUMBER).copy(value = "13")), 100.0)
    }

    @Test
    fun expression_with_countWithVariables() {
        val values = mapOf(
            "xxx1" to VariableValue(ValueType.NUMBER).copy(candidates = listOf("1", "2", "1")),
            "xxx2" to VariableValue(ValueType.NUMBER).copy(candidates = listOf("1", "2", "3")))
        assertCountWithVariables(values,0, 0, false)
        assertCountWithVariables(values,1, 0, true)
        assertCountWithVariables(values,2, 0, false)
        assertCountWithVariables(values,2, 2, true)
        assertCountWithVariables(values,2, 3, true)
        assertCountWithVariables(values,3, 3, false)
    }

    private fun assertCondition(expected: Any, expression: String) {
        assertEquals(expected, Expression(expression, ExpressionMode.ANDROID_CUSTOM_INTENT_EXPRESSION).evaluate())
    }

    private fun assertCondition(expression: String, values: Map<String, VariableValue>, expected: Any) {
        assertEquals(expected, evaluate(expression, values))
    }

    private fun assertCountWithVariables(values: Map<String, VariableValue>, a: Number, b: Number, expected: Boolean) {
        assertCondition("d2:condition('d2:countIfValue(#{xxx1},\"${a}\") + d2:countIfValue(#{xxx2},\"${b}\") >= 2', true, false)", values, expected)
    }
}