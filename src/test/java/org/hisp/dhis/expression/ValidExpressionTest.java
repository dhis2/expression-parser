package org.hisp.dhis.expression;

import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.eval.CalcNodeInterpreter;
import org.hisp.dhis.expression.syntax.Expr.ParseException;
import org.hisp.dhis.expression.syntax.ExpressionGrammar;
import org.hisp.dhis.expression.syntax.Parser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Port of the ANTLR {@code ValidExpressionTest}.
 */
class ValidExpressionTest {

    @Test
    void testValidExpression() {
        assertEquals( "a", evaluate( "'a'" ) );
        assertEquals( true, evaluate( "true" ) );
        assertEquals( "true", evaluate( "'true'" ) );
        assertEquals( 34.0, evaluate( "34" ) );
        assertEquals( "34", evaluate( "'34'" ) );
        assertEquals( "34.0", evaluate( "'34.0'" ) );
    }

    @Test
    void testInvalidSyntaxExpression1()
    {
        ParseException ex = assertThrows( ParseException.class, () ->  evaluate( "1+1+1 > 0 _ 1" ));
        assertEquals("Unexpected input character: '_'\n" +
                "\tat line:1 character:10\n" +
                "\t1+1+1 > 0 _ 1\n" +
                "\t          ^", ex.getMessage() );
    }

    @Test
    void testInvalidSyntaxExpression2()
    {
        ParseException ex = assertThrows( ParseException.class, () -> evaluate( "1 + 1 + a" ));
        assertEquals("Unknown function or constant: 'a'\n" +
                "\tat line:1 character:8\n" +
                "\t1 + 1 + a\n" +
                "\t        ^", ex.getMessage() );
    }

    @Test
    void testInvalidSyntaxExpression3()
    {
        ParseException ex = assertThrows( ParseException.class, () -> evaluate( "1 + 1  ( 2 + 2 )" ));
        assertEquals("Unexpected input character: '('\n" +
                "\tat line:1 character:7\n" +
                "\t1 + 1  ( 2 + 2 )\n" +
                "\t       ^", ex.getMessage() );
    }

    @Test
    void testExpressionWithValidSyntaxAndNotSupportedItem()
    {
        assertThrows( ParseException.class, () -> evaluate( "2 > #{not_supported}" ));
    }

    @Test
    void testExpressionWithValidSyntaxAndNotSupportedAttribute()
    {
        assertThrows( ParseException.class, () -> evaluate( "2 > A{not_supported}" ));
    }

    @Test
    void testExpressionWithValidSyntaxAndNotSupportedVariable()
    {
        ParseException ex = assertThrows( ParseException.class, () -> evaluate( "2 > V{not_supported}" ));
        assertEquals("Invalid ProgramVariable option: 'not_supported'\n" +
                "\toptions are: [analytics_period_end, analytics_period_start, completed_date, creation_date, current_date, due_date, enrollment_count, enrollment_date, enrollment_id, enrollment_status, environment, event_count, scheduled_event_count, event_date, scheduled_date, event_id, event_status, execution_date, incident_date, org_unit_count, org_unit, orgunit_code, program_name, program_stage_id, program_stage_name, sync_date, tei_count, value_count, zero_pos_value_count]\n" +
                "\tat line:1 character:6\n" +
                "\t2 > V{not_supported}\n" +
                "\t      ^-----------^", ex.getMessage());
    }

    private static Object evaluate( String expression )
    {
        Node<?> root = Parser.parse(expression, ExpressionGrammar.Fragments);
        return root.eval(new CalcNodeInterpreter());
    }
}
