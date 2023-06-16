package org.hisp.dhis.lib.expression;

import org.hisp.dhis.lib.expression.spi.IllegalExpressionException;
import org.hisp.dhis.lib.expression.spi.ParseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Port of the ANTLR {@code ValidExpressionTest}.
 */
class ValidExpressionTest {

    @Test
    void testValidExpression() {
        assertEquals("a", evaluate("'a'"));
        assertEquals(true, evaluate("true"));
        assertEquals("true", evaluate("'true'"));
        assertEquals(34.0, evaluate("34"));
        assertEquals("34", evaluate("'34'"));
        assertEquals("34.0", evaluate("'34.0'"));
    }

    @Test
    void testInvalidSyntaxExpression1() {
        ParseException ex = assertThrows(ParseException.class, () -> evaluate("1+1+1 > 0 _ 1"));
        assertEquals("""
                Unexpected input character: '_'
                \tat line:1 character:10
                \t1+1+1 > 0 _ 1
                \t          ^""", ex.getMessage());
    }

    @Test
    void testInvalidSyntaxExpression2() {
        ParseException ex = assertThrows(ParseException.class, () -> evaluate("1 + 1 + a"));
        assertEquals("""
                Unknown function or constant: 'a'
                \tat line:1 character:8
                \t1 + 1 + a
                \t        ^""", ex.getMessage());
    }

    @Test
    void testInvalidSyntaxExpression3() {
        ParseException ex = assertThrows(ParseException.class, () -> evaluate("1 + 1  ( 2 + 2 )"));
        assertEquals("""
                Unexpected input character: '('
                \tat line:1 character:7
                \t1 + 1  ( 2 + 2 )
                \t       ^""", ex.getMessage());
    }

    @Test
    void testExpressionWithValidSyntaxAndNotSupportedItem() {
        IllegalExpressionException ex = assertThrows(IllegalExpressionException.class, () -> evaluate("2 > #{not_supported}"));
        assertEquals("Unknown variable: 'not_supported'", ex.getMessage());
    }

    @Test
    void testExpressionWithValidSyntaxAndNotSupportedAttribute() {
        IllegalExpressionException ex = assertThrows(IllegalExpressionException.class, () -> evaluate("2 > A{not_supported}"));
        assertEquals("Unknown variable: 'not_supported'", ex.getMessage());
    }

    @Test
    void testExpressionWithValidSyntaxAndNotSupportedVariable() {
        ParseException ex = assertThrows(ParseException.class, () -> evaluate("2 > V{not_supported}"));
        assertEquals("""
                Invalid ProgramVariable option: 'not_supported'
                \toptions are: [analytics_period_end, analytics_period_start, completed_date, creation_date, current_date, due_date, enrollment_count, enrollment_date, enrollment_id, enrollment_status, environment, event_count, scheduled_event_count, event_date, scheduled_date, event_id, event_status, execution_date, incident_date, org_unit_count, org_unit, orgunit_code, program_name, program_stage_id, program_stage_name, sync_date, tei_count, value_count, zero_pos_value_count]
                \tat line:1 character:6
                \t2 > V{not_supported}
                \t      ^-----------^""", ex.getMessage());
    }

    private static Object evaluate(String expression) {
        return new Expression(expression, Expression.Mode.PROGRAM_INDICATOR_EXPRESSION).evaluate();
    }
}
