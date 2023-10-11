package org.hisp.dhis.lib.expression

import org.hisp.dhis.lib.expression.ast.ProgramVariable
import org.hisp.dhis.lib.expression.spi.QueryModifiers
import org.hisp.dhis.lib.expression.spi.Variable
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

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
            val expr = Expression(expression, Expression.Mode.PROGRAM_INDICATOR_EXPRESSION)
            return expr.collectProgramVariables()
        }
    }
}
