package org.hisp.dhis.lib.expression.eval

import kotlinx.datetime.LocalDate
import org.hisp.dhis.lib.expression.ast.*
import org.hisp.dhis.lib.expression.spi.DataItemType

/**
 * Extended visitor for [Node]s by their [NodeType].
 *
 * @author Jan Bernitt
 */
@Suppress("kotlin:S1186")
interface NodeVisitor {

    @Suppress("kotlin:S6530", "UNCHECKED_CAST")
    fun visitNode(node: Node<*>) {
        when (node.getType()) {
            NodeType.UNARY_OPERATOR -> visitUnaryOperator(node as Node<UnaryOperator>)
            NodeType.BINARY_OPERATOR -> visitBinaryOperator(node as Node<BinaryOperator>)
            NodeType.ARGUMENT -> visitArgument(node as Node<Int>)
            NodeType.PAR -> visitParentheses(node as Node<Unit>)
            NodeType.FUNCTION -> visitFunction(node as Node<NamedFunction>)
            NodeType.MODIFIER -> visitModifier(node as Node<DataItemModifier>)
            NodeType.DATA_ITEM -> visitDataItem(node as Node<DataItemType>)
            NodeType.VARIABLE -> visitVariable(node as Node<VariableType>)
            NodeType.BOOLEAN -> visitBoolean(node as Node<Boolean>)
            NodeType.UID -> visitUid(node as Node<String>)
            NodeType.DATE -> visitDate(node as Node<LocalDate>)
            NodeType.NULL -> visitNull(node as Node<Unit>)
            NodeType.NUMBER -> visitNumber(node as Node<Double>)
            NodeType.STRING -> visitString(node as Node<String>)
            NodeType.INTEGER -> visitInteger(node as Node<Int>)
            NodeType.IDENTIFIER -> visitIdentifier(node)
            NodeType.NAMED_VALUE -> visitNamedValue(node as Node<NamedValue>)
        }
    }

    fun visitParentheses(group: Node<Unit>) {}
    fun visitArgument(argument: Node<Int>) {}
    fun visitBinaryOperator(operator: Node<BinaryOperator>) {}
    fun visitUnaryOperator(operator: Node<UnaryOperator>) {}
    fun visitFunction(fn: Node<NamedFunction>) {}
    fun visitModifier(modifier: Node<DataItemModifier>) {}
    fun visitDataItem(item: Node<DataItemType>) {}
    fun visitVariable(variable: Node<VariableType>) {}

    /*
    Simple nodes:
     */
    fun visitNamedValue(value: Node<NamedValue>) {}
    fun visitNumber(value: Node<Double>) {}
    fun visitInteger(value: Node<Int>) {}
    fun visitBoolean(value: Node<Boolean>) {}
    fun visitNull(value: Node<Unit>) {}
    fun visitString(value: Node<String>) {}
    fun visitIdentifier(value: Node<*>) {
        // identifier nodes use both enums or String values so at this point we can't say what value we got
    }

    fun visitUid(value: Node<String>) {}
    fun visitDate(value: Node<LocalDate>) {}
}
