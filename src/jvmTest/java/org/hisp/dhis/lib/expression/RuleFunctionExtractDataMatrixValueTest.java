package org.hisp.dhis.lib.expression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test of the {@code d2:extractDataMatrixValue} function.
 * <p>
 * Translated from existing test of same name in rule-engine.
 *
 * @author Jan Bernitt
 */
class RuleFunctionExtractDataMatrixValueTest {

    @Test
    void throw_argument_exception_if_value_is_not_gs1() {
        assertThrows(IllegalArgumentException.class, () -> extractDataMatrixValue("serial number", "testingvalue"));
    }

    @Test
    void throw_argument_exception_if_key_is_not_valid() {
        String value = "]d2\u001D01084700069915412110081996195256\u001D10DXB2005\u001D17220228";
        assertThrows(IllegalArgumentException.class, () -> extractDataMatrixValue("serial numb", value));
    }

    @Test
    void return_gs1_value_if_named_key_is_in_value() {
        String value = "]d2\u001D01084700069915412110081996195256\u001D10DXB2005\u001D17220228";
        assertEquals("10081996195256", extractDataMatrixValue("serial number", value));
    }

    @Test
    void return_gs1_value_if_numeric_key_is_in_value() {
        String value = "]d2\u001D01084700069915412110081996195256\u001D10DXB2005\u001D17220228";
        assertEquals("10081996195256", extractDataMatrixValue("21", value));
    }

    @Test
    void return_gs1_value_if_key_is_in_value() {
        String value = "]d201084700069915412110081996195256\u001D10DXB2005\u001D17220228";
        assertEquals("10081996195256", extractDataMatrixValue("serial number", value));
    }

    @Test
    void throw_exception_if_key_is_not_in_value() {
        String value = "]d2\u001D01084700069915412110081996195256\u001D10DXB2005\u001D17220228";
        assertThrows(IllegalArgumentException.class, () -> extractDataMatrixValue("production date", value));
    }

    private static String extractDataMatrixValue(String key, String value) {
        String expression = String.format("d2:extractDataMatrixValue('%s', '%s')", key, value);
        return (String) new Expression(expression, Expression.Mode.RULE_ENGINE_ACTION).evaluate();
    }
}
