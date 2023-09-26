package org.hisp.dhis.lib.expression

import org.hisp.dhis.lib.expression.spi.ExpressionData
import org.hisp.dhis.lib.expression.spi.ValueType
import org.hisp.dhis.lib.expression.spi.VariableValue
import org.hisp.dhis.lib.expression.util.RuleVariableValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

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
        assertHasValue("d2:hasValue(#{nonexisting})", java.util.Map.of(), false)
    }

    @Test
    fun return_false_for_existing_variable_without_value() {
        assertHasValue(
            "d2:hasValue(#{non_value_var})",
            Collections.singletonMap<String, VariableValue>("non_value_var", null),
            false)
    }

    @Test
    fun return_true_for_existing_variable_with_value() {
        assertHasValue(
            "d2:hasValue(#{with_value_var})",
            java.util.Map.of<String, VariableValue>(
                "with_value_var",
                RuleVariableValue(ValueType.STRING).copy(value = "value")),
            true)
    }

    private fun assertHasValue(expression: String, values: Map<String, VariableValue>, expected: Boolean) {
        val data: ExpressionData = ExpressionData().copy(programRuleVariableValues = values)
        val actual = Expression(expression, Expression.Mode.RULE_ENGINE_ACTION).evaluate(
            { _: String? -> null }, data)
        assertEquals(expected, actual)
    }
}
