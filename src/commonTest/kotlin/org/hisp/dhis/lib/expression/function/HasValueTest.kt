package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.spi.ValueType
import org.hisp.dhis.lib.expression.spi.VariableValue

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test of the `d2:hasValue` function.
 *
 * Translated from existing test of same name in rule-engine.
 *
 * @author Jan Bernitt
 */
internal class HasValueTest : AbstractVariableBasedTest() {
    @Test
    fun return_false_for_non_existing_variable_with_UID() {
        // 'nonexisting' is treated as valid UID
        assertHasValue("d2:hasValue(#{nonexisting})", mapOf(), false)
    }

    @Test
    fun return_false_for_non_existing_variable_with_name() {
        assertHasValue("d2:hasValue(#{variable-name})", mapOf(), false)
    }


    @Test
    fun return_false_for_existing_variable_without_value() {
        assertHasValue(
            "d2:hasValue(#{non_value_var})",
            mapOf("non_value_var" to VariableValue(ValueType.STRING)), false)
    }

    @Test
    fun return_true_for_existing_variable_with_value() {
        assertHasValue(
            "d2:hasValue(#{with_value_var})",
            mapOf("with_value_var" to VariableValue(ValueType.STRING).copy(value = "value")), true)
    }

    private fun assertHasValue(expression: String, values: Map<String, VariableValue>, expected: Boolean) {
        assertEquals(expected, evaluate(expression, values))
    }
}
