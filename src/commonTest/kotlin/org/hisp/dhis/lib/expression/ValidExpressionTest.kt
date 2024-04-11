package org.hisp.dhis.lib.expression

import org.hisp.dhis.lib.expression.spi.IllegalExpressionException
import org.hisp.dhis.lib.expression.spi.ParseException

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Port of the ANTLR `ValidExpressionTest`.
 */
internal class ValidExpressionTest {
    @Test
    fun testValidExpression() {
        assertEquals("a", evaluate("'a'"))
        assertEquals(true, evaluate("true"))
        assertEquals("true", evaluate("'true'"))
        assertEquals(34.0, evaluate("34"))
        assertEquals("34", evaluate("'34'"))
        assertEquals("34.0", evaluate("'34.0'"))
    }

    @Test
    fun testInvalidSyntaxExpression1() {
        val ex = assertFailsWith(ParseException::class) { evaluate("1+1+1 > 0 _ 1") }
        assertEquals(
            """
                Unexpected input character: '_'
                ${'\t'}at line:1 character:10
                ${'\t'}1+1+1 > 0 _ 1
                ${'\t'}          ^
                """.trimIndent(), ex.message)
    }

    @Test
    fun testInvalidSyntaxExpression2() {
        val ex = assertFailsWith(ParseException::class) { evaluate("1 + 1 + a") }
        assertEquals(
            """
                Unknown function or constant: 'a'
                ${'\t'}at line:1 character:8
                ${'\t'}1 + 1 + a
                ${'\t'}        ^
                """.trimIndent(), ex.message)
    }

    @Test
    fun testInvalidSyntaxExpression3() {
        val ex = assertFailsWith(ParseException::class) { evaluate("1 + 1  ( 2 + 2 )") }
        assertEquals(
            """
                Unexpected input character: '('
                ${'\t'}at line:1 character:7
                ${'\t'}1 + 1  ( 2 + 2 )
                ${'\t'}       ^
                """.trimIndent(), ex.message)
    }

    @Test
    fun testExpressionWithValidSyntaxAndNotSupportedItem() {
        val ex = assertFailsWith(IllegalExpressionException::class) { evaluate("2 > #{not_supported}") }
        assertEquals("Unknown variable: 'not_supported'", ex.message)
    }

    @Test
    fun testExpressionWithValidSyntaxAndNotSupportedAttribute() {
        val ex = assertFailsWith(IllegalExpressionException::class) { evaluate("2 > A{not_supported}") }
        assertEquals("Unknown variable: 'not_supported'", ex.message)
    }

    @Test
    fun testExpressionWithValidSyntaxAndNotSupportedVariable() {
        val ex = assertFailsWith(ParseException::class) { evaluate("2 > V{not_supported}") }
        assertEquals(
            """
                Invalid ProgramVariable option: 'not_supported'
                ${'\t'}options are: [analytics_period_end, analytics_period_start, completed_date, creation_date, current_date, due_date, enrollment_count, enrollment_date, enrollment_id, enrollment_status, environment, event_count, scheduled_event_count, event_date, scheduled_date, event_id, event_status, execution_date, incident_date, org_unit_count, org_unit, orgunit_code, program_name, program_stage_id, program_stage_name, sync_date, tei_count, value_count, zero_pos_value_count]
                ${'\t'}at line:1 character:6
                ${'\t'}2 > V{not_supported}
                ${'\t'}      ^-----------^
                """.trimIndent(), ex.message)
    }

    private fun evaluate(expression: String): Any? {
        return Expression(expression, ExpressionMode.PROGRAM_INDICATOR_EXPRESSION).evaluate()
    }
}
