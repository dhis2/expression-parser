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

    @Test
    fun testProgramRuleVariable_InstantType() {
        val data = ExpressionData().copy(
            programRuleVariableValues = mapOf(
                Pair(
                    "start",
                    VariableValue(ValueType.DATE, "2020-01-01T12:15:00Z", listOf(), null)),
                Pair(
                    "end",
                    VariableValue(ValueType.DATE, "2020-01-01T12:19:00Z", listOf(), null)),
            ))

        fun evaluate(expression: String): Double {
            return (Expression(expression, RULE_ENGINE_CONDITION).evaluate(data) as Number).toDouble()
        }
        assertEquals(4.0, evaluate("d2:minutesBetween(#{start}, #{end})"))
        assertEquals(34.0, evaluate("d2:minutesBetween(#{start}, \"2020-01-01 12:49\")"))
        assertEquals(13.0, evaluate("d2:daysBetween(#{start}, \"2020-01-14\")"))
        assertEquals(0.0, evaluate("d2:minutesBetween(\"2020-01-01T12:00:00.000011\", \"2020-01-01T12:00:01.000010\")"))
        assertEquals(0.1 / 6, evaluate("d2:minutesBetween(\"2020-01-01T12:00:00.000011\", \"2020-01-01T12:00:01.000011\")"))
        assertEquals(0.0, evaluate("d2:minutesBetween(#{start}, \"2020-01-01T12:15:00.000000001Z\")"))
        assertEquals(0.0, evaluate("d2:minutesBetween(#{start}, \"2020-01-01T12:15:00.000001Z\")"))
        assertEquals(0.0, evaluate("d2:minutesBetween(#{start}, \"2020-01-01T12:15:00.000001\")"))
        assertEquals(0.0, evaluate("d2:minutesBetween(#{start}, \"2020-01-01T12:15:00.0001Z\")"))
        assertEquals(0.0, evaluate("d2:minutesBetween(#{start}, \"2020-01-01T12:15:00.0001\")"))
        assertEquals(0.0, evaluate("d2:minutesBetween(#{start}, \"2020-01-01T12:15:00.001Z\")"))
        assertEquals(0.0, evaluate("d2:minutesBetween(#{start}, \"2020-01-01T12:15:00.001\")"))
        assertEquals(0.1, evaluate("d2:minutesBetween(#{start}, \"2020-01-01T12:15:06Z\")"))
        assertEquals(0.2, evaluate("d2:minutesBetween(#{start}, \"2020-01-01T12:15:12\")"))
        assertEquals(35.0, evaluate("d2:minutesBetween(#{start}, \"2020-01-01T12:50Z\")"))
        assertEquals(32.0, evaluate("d2:minutesBetween(#{start}, \"2020-01-01T12:47\")"))
        assertEquals(45.0, evaluate("d2:minutesBetween(#{start}, \"2020-01-01T13Z\")"))
        assertEquals(-15.0, evaluate("d2:minutesBetween(#{start}, \"2020-01-01T12\")"))
        assertEquals(10.3, evaluate("d2:minutesBetween(#{start}, \"2020-01-01 12:25:18Z\")"))
    }

    private fun assertHasStructure(expected: TNode, expression: String) {
        val root = Parser.parse(expression, ExpressionGrammar.ProgramIndicatorExpressionMode, true)
        assertEquals(TNode(NodeType.PAR, mutableListOf(expected)), TNode.of(root))
    }
}
