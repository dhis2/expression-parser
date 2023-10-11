package org.hisp.dhis.lib.expression

import kotlin.test.assertEquals
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * Test of the `d2:extractDataMatrixValue` function.
 *
 *
 * Translated from existing test of same name in rule-engine.
 *
 * @author Jan Bernitt
 */
internal class RuleFunctionExtractDataMatrixValueTest {
    @Test
    fun throw_argument_exception_if_value_is_not_gs1() {
        assertFailsWith(IllegalArgumentException::class) { extractDataMatrixValue("serial number", "testingvalue") }
    }

    @Test
    fun throw_argument_exception_if_key_is_not_valid() {
        val value = "]d2\u001D01084700069915412110081996195256\u001D10DXB2005\u001D17220228"
        assertFailsWith(IllegalArgumentException::class) { extractDataMatrixValue("serial numb", value) }
    }

    @Test
    fun return_gs1_value_if_named_key_is_in_value() {
        val value = "]d2\u001D01084700069915412110081996195256\u001D10DXB2005\u001D17220228"
        assertEquals("10081996195256", extractDataMatrixValue("serial number", value))
    }

    @Test
    fun return_gs1_value_if_numeric_key_is_in_value() {
        val value = "]d2\u001D01084700069915412110081996195256\u001D10DXB2005\u001D17220228"
        assertEquals("10081996195256", extractDataMatrixValue("21", value))
    }

    @Test
    fun return_gs1_value_if_key_is_in_value() {
        val value = "]d201084700069915412110081996195256\u001D10DXB2005\u001D17220228"
        assertEquals("10081996195256", extractDataMatrixValue("serial number", value))
    }

    @Test
    fun throw_exception_if_key_is_not_in_value() {
        val value = "]d2\u001D01084700069915412110081996195256\u001D10DXB2005\u001D17220228"
        assertFailsWith(IllegalArgumentException::class) { extractDataMatrixValue("production date", value) }
    }

    companion object {
        private fun extractDataMatrixValue(key: String, value: String): String {
            val expression = "d2:extractDataMatrixValue('$key', '$value')"
            return Expression(expression, Expression.Mode.RULE_ENGINE_ACTION).evaluate() as String
        }
    }
}
