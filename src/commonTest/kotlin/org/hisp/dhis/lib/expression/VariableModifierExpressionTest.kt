package org.hisp.dhis.lib.expression

import org.hisp.dhis.lib.expression.spi.ProgramVariable
import org.hisp.dhis.lib.expression.spi.QueryModifiers
import org.hisp.dhis.lib.expression.spi.Variable
import org.hisp.dhis.lib.expression.Expression.Mode

import kotlin.test.Test
import kotlin.test.assertEquals

internal class VariableModifierExpressionTest {
    @Test
    fun testStageOffset() {
        val expected: QueryModifiers = QueryModifiers().copy(stageOffset = 12)
        assertEquals(
            setOf(Variable(ProgramVariable.event_count, expected)),
            evaluate("V{event_count}.stageOffset(12)"))
    }

    @Test
    fun testMultiStageOffset() {
        // stage offsets add up
        val expected: QueryModifiers = QueryModifiers().copy(stageOffset = 3)
        assertEquals(
            setOf(Variable(ProgramVariable.event_count, expected)),
            evaluate("(V{event_count}.stageOffset(1)).stageOffset(2)"))
    }

    companion object {
        private fun evaluate(expression: String): Set<Variable> {
            val expr = Expression(expression, Mode.PROGRAM_INDICATOR_EXPRESSION)
            return expr.collectProgramVariables()
        }
    }
}
