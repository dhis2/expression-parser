package org.hisp.dhis.lib.expression

import kotlinx.datetime.LocalDate
import org.hisp.dhis.lib.expression.ExpressionMode.RULE_ENGINE_CONDITION
import org.hisp.dhis.lib.expression.ast.NodeType
import org.hisp.dhis.lib.expression.spi.ExpressionData
import org.hisp.dhis.lib.expression.spi.ValueType
import org.hisp.dhis.lib.expression.spi.VariableValue
import org.hisp.dhis.lib.expression.syntax.ExpressionGrammar
import org.hisp.dhis.lib.expression.syntax.Parser
import org.hisp.dhis.lib.expression.util.TNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests that expressions with variable names are parsed into the expected AST structure.
 *
 * @author Jan Bernitt
 */
internal class VariableExpressionTest {
    @Test
    fun testProgramVariable() {
        assertHasStructure(
            TNode.ofLevels(NodeType.VARIABLE, NodeType.IDENTIFIER),
            "V{current_date}")
    }

    @Test
    fun testProgramRuleVariable_Hash() {
        assertHasStructure(
            TNode.ofLevels(NodeType.VARIABLE, NodeType.IDENTIFIER),
            "#{varname}")
    }

    @Test
    fun testProgramRuleVariable_A() {
        assertHasStructure(
            TNode.ofLevels(NodeType.VARIABLE, NodeType.IDENTIFIER),
            "A{varname}")
    }

    @Test
    fun testProgramRuleVariable_SingleQuoted() {
        assertHasStructure(
            TNode.ofLevels(NodeType.FUNCTION, NodeType.ARGUMENT, NodeType.VARIABLE, NodeType.STRING),
            "d2:maxValue('varname')")
    }

    @Test
    fun testProgramRuleVariable_DoubleQuoted() {
        assertHasStructure(
            TNode.ofLevels(NodeType.FUNCTION, NodeType.ARGUMENT, NodeType.VARIABLE, NodeType.STRING),
            "d2:maxValue(\"varname\")")
    }

    @Test
    fun testProgramRuleVariable_DateCompare() {
        val data = ExpressionData().copy(
            programRuleVariableValues = mapOf(
                Pair(
                    "event_date",
                    VariableValue(ValueType.DATE, LocalDate.fromEpochDays(11).toString(), listOf(), null)),
                Pair(
                    "current_date",
                    VariableValue(ValueType.DATE, LocalDate.fromEpochDays(10).toString(), listOf(), null)),
            ))
        val expr = Expression("V{event_date} > V{current_date}", RULE_ENGINE_CONDITION)
        assertTrue(expr.evaluate(data) as Boolean)
        val expr2 = Expression("V{event_date} - V{current_date}", RULE_ENGINE_CONDITION)
        assertEquals(1.0, expr2.evaluate(data) as Double)
    }

    private fun assertHasStructure(expected: TNode, expression: String) {
        val root = Parser.parse(expression, ExpressionGrammar.ProgramIndicatorExpressionMode, true)
        assertEquals(TNode(NodeType.PAR, mutableListOf(expected)), TNode.of(root))
    }
}
