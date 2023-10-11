package org.hisp.dhis.lib.expression.eval

import org.hisp.dhis.lib.expression.ast.Node
import org.hisp.dhis.lib.expression.ast.NodeType
import org.hisp.dhis.lib.expression.spi.DataItemType
import org.hisp.dhis.lib.expression.spi.ExpressionData
import org.hisp.dhis.lib.expression.spi.Issues

/**
 * Validates a [Node] using custom logic.
 *
 * @author Jan Bernitt
 */
fun interface NodeValidator {

    fun validate(root: Node<*>, issues: Issues, data: ExpressionData)

    companion object {
        /*
        Additional AST validation...
        */
        private val VARIABLE_MUST_EXIST: NodeValidator = NodeValidator { root: Node<*>, issues: Issues, data: ExpressionData ->
            root.visit(
                NodeType.VARIABLE
            ) { node ->
                val name: String = node.child(0).getRawValue()
                if (!data.programRuleVariableValues.containsKey(name)) {
                    issues.addError(node, "Unknown variable: `$name`")
                }
            }
        }
        private val CONSTANT_MUST_EXIST: NodeValidator = NodeValidator { root: Node<*>, issues: Issues, data: ExpressionData ->
            root.visit(
                NodeType.DATA_ITEM
            ) { node ->
                if (node.getValue() !== DataItemType.CONSTANT) return@visit
                val key = node.toDataItem()!!.getKey()
                if (!data.programRuleVariableValues.containsKey(key)) {
                    issues.addError(node, "Unknown constant: `$key`")
                }
            }
        }

        /*
        Modes...
        */
        val RuleEngineMode = listOf(
            VARIABLE_MUST_EXIST,
            CONSTANT_MUST_EXIST
        )
    }
}
