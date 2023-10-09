package org.hisp.dhis.lib.expression.eval

import org.hisp.dhis.lib.expression.ast.*
import org.hisp.dhis.lib.expression.spi.DataItemType
import java.time.LocalDate

/**
 * A [NodeInterpreter] interprets a root [Node] to calculate or derive some value from it.
 *
 * @param <T> type of the result of the interpretation
 * @author Jan Bernitt
</T> */
interface NodeInterpreter<T> : (Node<*>) -> T? {

    @Suppress("kotlin:S6530", "UNCHECKED_CAST")
    override fun invoke(node: Node<*>): T? {
        return when (node.getType()) {
            NodeType.UNARY_OPERATOR -> evalUnaryOperator(node as Node<UnaryOperator>)
            NodeType.BINARY_OPERATOR -> evalBinaryOperator(node as Node<BinaryOperator>)
            NodeType.ARGUMENT -> evalArgument(node as Node<Int>)
            NodeType.PAR -> evalParentheses(node as Node<Unit>)
            NodeType.FUNCTION -> evalFunction(node as Node<NamedFunction>)
            NodeType.MODIFIER -> evalModifier(node as Node<DataItemModifier>)
            NodeType.DATA_ITEM -> evalDataItem(node as Node<DataItemType>)
            NodeType.VARIABLE -> evalVariable(node as Node<VariableType>)
            NodeType.BOOLEAN -> evalBoolean(node as Node<Boolean>)
            NodeType.UID -> evalUid(node as Node<String>)
            NodeType.DATE -> evalDate(node as Node<LocalDate>)
            NodeType.NULL -> evalNull(node as Node<Unit>)
            NodeType.NUMBER -> evalNumber(node as Node<Double>)
            NodeType.STRING -> evalString(node as Node<String>)
            NodeType.INTEGER -> evalInteger(node as Node<Int>)
            NodeType.IDENTIFIER -> evalIdentifier(node as Node<Any>)
            NodeType.NAMED_VALUE -> evalNamedValue(node as Node<NamedValue>)
        }
    }

    fun evalParentheses(group: Node<Unit>): T? {
        return group.child(0).eval(this)
    }

    fun evalArgument(argument: Node<Int>): T? {
        return argument.child(0).eval(this)
    }

    fun evalBinaryOperator(operator: Node<BinaryOperator>): T?
    fun evalUnaryOperator(operator: Node<UnaryOperator>): T?
    fun evalFunction(fn: Node<NamedFunction>): T?
    fun evalModifier(modifier: Node<DataItemModifier>): T
    fun evalDataItem(item: Node<DataItemType>): T?
    fun evalVariable(variable: Node<VariableType>): T?

    /*
    Simple nodes:
     */
    fun evalNamedValue(value: Node<NamedValue>): T
    fun evalNumber(value: Node<Double>): T
    fun evalInteger(value: Node<Int>): T
    fun evalBoolean(value: Node<Boolean>): T
    fun evalNull(value: Node<Unit>): T?
    fun evalString(value: Node<String>): T
    fun evalIdentifier(value: Node<Any>): T
    fun evalUid(value: Node<String>): T
    fun evalDate(value: Node<LocalDate>): T
}
