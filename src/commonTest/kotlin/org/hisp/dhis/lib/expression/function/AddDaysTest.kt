package org.hisp.dhis.lib.expression.function

import kotlinx.datetime.LocalDate
import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.spi.IllegalExpressionException
import org.hisp.dhis.lib.expression.ExpressionMode

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Test of the `d2:addDays` function.
 *
 * Translated from existing test of same name in rule-engine.
 *
 * @author Jan Bernitt
 */
internal class AddDaysTest {
    @Test
    fun return_new_date_with_days_added() {
        assertEquals(LocalDate.parse("2011-01-07"), evaluate("d2:addDays('2011-01-01', 6.0)"))
        assertEquals(LocalDate.parse("2010-10-11"), evaluate("d2:addDays('2010-10-10', 1)"))
        assertEquals(LocalDate.parse("2010-10-11"), evaluate("d2:addDays('2010-10-10', 1.3)"))
        assertEquals(LocalDate.parse("2010-11-01"), evaluate("d2:addDays('2010-10-31', 1)"))
        assertEquals(LocalDate.parse("2011-01-01"), evaluate("d2:addDays('2010-12-01', 31)"))
    }

    @Test
    fun throw_runtime_exception_if_first_argument_is_invalid() {
        assertIllegalExpression(
            "d2:addDays('bad date', 6)",
            "Failed to coerce value 'bad date' (String) to Date in expression: 'bad date'")
    }

    @Test
    fun throw_illegal_argument_exception_if_second_argument_is_invalid() {
        assertIllegalExpression(
            "d2:addDays('2010-01-01', 'bad number')",
            "Failed to coerce value 'bad number' (String) to Double in expression: 'bad number'")
    }

    @Test
    fun throw_illegal_argument_exception_if_first_and_second_argument_is_invalid() {
        assertIllegalExpression(
            "d2:addDays('bad date', 'bad number')",
            "Failed to coerce value 'bad date' (String) to Date in expression: 'bad date'")
    }

    private fun assertIllegalExpression(expression: String, expectedMessage: String) {
        val ex = assertFailsWith(IllegalExpressionException::class) { evaluate(expression) }
        assertEquals(expectedMessage, ex.message)
    }

    companion object {
        private fun evaluate(expression: String): LocalDate {
            return Expression(expression, ExpressionMode.RULE_ENGINE_ACTION).evaluate() as LocalDate
        }
    }
}
