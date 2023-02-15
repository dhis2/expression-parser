package org.hisp.dhis.lib.expression.util;

import org.hisp.dhis.lib.expression.Expression;
import org.hisp.dhis.lib.expression.spi.ValueType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BinaryOperatorExpressionTest {

    @Test
    void testOperatorPrecedence() {
        // or has less precedence than >
        // if that would be false the expression would be invalid or not compute to a boolean
        Expression expression = new Expression("d2:hasValue(#{test_var_two}) || d2:count(#{test_var_one}) > 0",
                Expression.Mode.RULE_ENGINE_CONDITION);
        assertDoesNotThrow(() -> expression.validate(Map.of(
                "test_var_one", ValueType.NUMBER,
                "test_var_two", ValueType.STRING)));
        assertEquals("d2:hasValue(#{test_var_two}) || d2:count(#{test_var_one}) > 0", expression.normalise());
    }
}
