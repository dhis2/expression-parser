package org.hisp.dhis.lib.expression

import org.hisp.dhis.lib.expression.spi.ExpressionData
import org.hisp.dhis.lib.expression.spi.ValueType
import org.hisp.dhis.lib.expression.spi.VariableValue
import org.hisp.dhis.lib.expression.util.RuleVariableValue
import org.hisp.dhis.lib.expression.Expression.Mode

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test of the `d2:hasValue` function.
 *
 *
 * Translated from existing test of same name in rule-engine.
 *
 * @author Jan Bernitt
 */
internal class RuleFunctionHasValueTest {
    @Test
    fun return_false_for_non_existing_variable() {
        assertHasValue("d2:hasValue(#{nonexisting})", mapOf(), false)
    }

    @Test
    fun return_false_for_existing_variable_without_value() {
        assertHasValue(
            "d2:hasValue(#{non_value_var})",
            mapOf("non_value_var" to RuleVariableValue(ValueType.STRING)), false)
    }

    @Test
    fun return_true_for_existing_variable_with_value() {
        assertHasValue(
            "d2:hasValue(#{with_value_var})",
            mapOf("with_value_var" to RuleVariableValue(ValueType.STRING).copy(value = "value")), true)
    }

    private fun assertHasValue(expression: String, values: Map<String, VariableValue>, expected: Boolean) {
        val data: ExpressionData = ExpressionData().copy(programRuleVariableValues = values)
        val actual = Expression(expression, Mode.RULE_ENGINE_ACTION).evaluate(
            { _: String? -> null }, data)
        assertEquals(expected, actual)
    }
}