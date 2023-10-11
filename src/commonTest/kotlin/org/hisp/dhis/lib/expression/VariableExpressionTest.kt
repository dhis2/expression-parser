package org.hisp.dhis.lib.expression

import org.hisp.dhis.lib.expression.ast.NodeType
import org.hisp.dhis.lib.expression.syntax.ExpressionGrammar
import org.hisp.dhis.lib.expression.syntax.Parser
import org.hisp.dhis.lib.expression.util.TNode
import kotlin.test.Test
import kotlin.test.assertEquals

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

    private fun assertHasStructure(expected: TNode, expression: String) {
        val root = Parser.parse(expression, ExpressionGrammar.ProgramIndicatorExpressionMode, true)
        assertEquals(TNode(NodeType.PAR, mutableListOf(expected)), TNode.of(root))
    }
}
