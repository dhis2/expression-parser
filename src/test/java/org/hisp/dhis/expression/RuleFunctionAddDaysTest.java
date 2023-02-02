package org.hisp.dhis.expression;

import org.hisp.dhis.expression.spi.IllegalExpressionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test of the {@code d2:ceil} function.
 *
 * Translated from existing test of same name in rule-engine.
 *
 * @author Jan Bernitt
 */
class RuleFunctionAddDaysTest {

    @Test
    void return_new_date_with_days_added()
    {
        assertEquals(LocalDate.parse("2011-01-07"), evaluate("d2:addDays('2011-01-01', 6.0)"));
        assertEquals(LocalDate.parse("2010-10-11"), evaluate("d2:addDays('2010-10-10', 1)"));
        assertEquals(LocalDate.parse("2010-10-11"), evaluate("d2:addDays('2010-10-10', 1.3)"));
        assertEquals(LocalDate.parse("2010-11-01"), evaluate("d2:addDays('2010-10-31', 1)"));
        assertEquals(LocalDate.parse("2011-01-01"), evaluate("d2:addDays('2010-12-01', 31)"));
    }

    @Test
    void throw_runtime_exception_if_first_argument_is_invalid()
    {
        assertIllegalExpression("d2:addDays('bad date', 6)",
                "Failed to coerce value 'bad date' (String) to LocalDate: Text 'bad date' could not be parsed at index 0\n" +
                        "\t in expression: 'bad date'");
    }

    @Test
    void throw_illegal_argument_exception_if_second_argument_is_invalid()
    {
        assertIllegalExpression("d2:addDays('2010-01-01', 'bad number')",
                "Failed to coerce value 'bad number' (String) to Double: For input string: \"bad number\"\n" +
                        "\t in expression: 'bad number'");
    }

    @Test
    void throw_illegal_argument_exception_if_first_and_second_argument_is_invalid()
    {
        assertIllegalExpression("d2:addDays('bad date', 'bad number')",
                "Failed to coerce value 'bad date' (String) to LocalDate: Text 'bad date' could not be parsed at index 0\n" +
                        "\t in expression: 'bad date'");
    }

    private void assertIllegalExpression(String expression, String expectedMessage) {
        IllegalExpressionException ex = assertThrows(IllegalExpressionException.class,
                () -> evaluate(expression));
        assertEquals(expectedMessage, ex.getMessage());
    }

    private static LocalDate evaluate(String expression) {
        return (LocalDate) new Expression(expression, Expression.Mode.RULE_ENGINE).evaluate();
    }
}
