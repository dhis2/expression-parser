package org.hisp.dhis.lib.expression;

import org.hisp.dhis.lib.expression.spi.ExpressionData;
import org.hisp.dhis.lib.expression.spi.VariableValue;
import org.hisp.dhis.lib.expression.util.RuleVariableValue;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test of the {@code d2:hasValue} function.
 *
 * Translated from existing test of same name in rule-engine.
 *
 * @author Jan Bernitt
 */
class RuleFunctionHasValueTest {

    @Test
    void return_false_for_non_existing_variable()
    {
        assertHasValue( "d2:hasValue(#{nonexisting})", Map.of(), false );
    }

    @Test
    void return_false_for_existing_variable_without_value()
    {
        assertHasValue( "d2:hasValue(#{non_value_var})", singletonMap("non_value_var", null), false );
    }

    @Test
    void return_true_for_existing_variable_with_value()
    {
        assertHasValue( "d2:hasValue(#{with_value_var})",
                Map.of("with_value_var", RuleVariableValue.of().value("value").build()), true );
    }

    private void assertHasValue( String expression, Map<String, VariableValue> values, Boolean expected )
    {
        ExpressionData data = ExpressionData.builder().programRuleVariableValues(values).build();
        Object actual = new Expression(expression, Expression.Mode.RULE_ENGINE).evaluate(name -> null, data);
        assertEquals(expected, actual);
    }
}
