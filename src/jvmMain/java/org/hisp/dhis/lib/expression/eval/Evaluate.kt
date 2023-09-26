package org.hisp.dhis.lib.expression.eval

import org.hisp.dhis.lib.expression.ast.Node
import org.hisp.dhis.lib.expression.ast.NodeType
import org.hisp.dhis.lib.expression.ast.Nodes.VariableNode
import org.hisp.dhis.lib.expression.ast.VariableType
import org.hisp.dhis.lib.expression.spi.*
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.collections.HashSet

/**
 * This is the exposed API of the evaluation package. It contains all high level functions to turn a [Node]-tree
 * into some value.
 *
 * @author Jan Bernitt
 */
class Evaluate private constructor() {

    companion object {
        /*
    Main functions to compute a result
     */
        @Throws(IllegalExpressionException::class)
        fun evaluate(root: Node<*>, functions: ExpressionFunctions, data: ExpressionData): Any? {
            val value = root.eval(EvaluateFunction(functions, data))
            return if (value is VariableValue) value.valueOrDefault() else value
        }

        fun normalise(root: Node<*>): String {
            return DescribeConsumer.toNormalisedExpression(root)
        }

        fun regenerate(root: Node<*>, dataItemValues: Map<DataItem, Number>): String {
            return DescribeConsumer.toValueExpression(root, dataItemValues)
        }

        fun describe(root: Node<*>, displayNames: Map<String, String>): String {
            return DescribeConsumer.toDisplayExpression(root, displayNames)
        }

        fun validate(
            root: Node<*>,
            data: ExpressionData,
            validators: List<NodeValidator>,
            resultTypes: Set<ValueType>
        ) {
            val issues = Issues()
            // enhance AST with variable type information
            root.visit(NodeType.VARIABLE) { variable ->
                val value = data.programRuleVariableValues[variable.child(0).getRawValue()]
                if (value != null) {
                    (variable as VariableNode).setActualValueType(value.valueType())
                }
            }

            // type check
            root.visit(TypeCheckingConsumer(issues))

            // check result type
            val actualResultType = root.getValueType()
            if (actualResultType !== ValueType.MIXED && actualResultType !== ValueType.SAME && !resultTypes.contains(
                    actualResultType)) {
                issues.addError(root, String.format("Expression must result in one of the types %s but was: %s",
                    resultTypes.stream().map { obj: ValueType -> obj.name }.collect(Collectors.joining(", ")),
                    actualResultType))
            }

            // AST and data dependent validations
            validators.forEach(Consumer { v: NodeValidator -> v.validate(root, issues, data) })
            issues.throwIfErrorsOrWarnings()
        }

        /*
    Support functions to collect identifiers to supply values to main functions
     */
        fun collectDataItems(root: Node<*>): Set<DataItem> {
            return root.aggregate(
                HashSet(),
                Node<*>::toDataItem,
                { set: MutableSet<DataItem>, e: DataItem? -> set.add(e!!) }
            ) { node: Node<*> -> node.getType() === NodeType.DATA_ITEM }
        }

        fun collectDataItems(root: Node<*>, vararg ofTypes: DataItemType): Set<DataItem> {
            //TODO need to add subExpression modifier SQL in case that is present
            val filter = setOf(ofTypes)
            return root.aggregate(
                HashSet(),
                Node<*>::toDataItem,
                { set: MutableSet<DataItem>, e: DataItem? -> set.add(e!!) }
            ) { node: Node<*> -> node.getType() === NodeType.DATA_ITEM && filter.contains(node.getValue()) }
        }

        fun collectVariableNames(root: Node<*>, type: VariableType): Set<String> {
            return root.aggregate(
                HashSet(),
                { node: Node<*> -> node.child(0).getRawValue() },
                MutableSet<String>::add
            ) { node: Node<*> -> node.getType() === NodeType.VARIABLE && node.getValue() === type }
        }

        fun collectVariables(root: Node<*>, type: VariableType): Set<Variable> {
            return root.aggregate(
                HashSet(),
                Node<*>::toVariable,
                { set: MutableSet<Variable>, e: Variable? -> set.add(e!!) }
            ) { node: Node<*> -> node.getType() === NodeType.VARIABLE && node.getValue() === type }
        }

        fun collectUIDs(root: Node<*>): Set<ID> {
            return root.aggregate(
                HashSet(), Node<*>::toIDs,
                { set: HashSet<ID>, ids: Stream<ID> ->
                    ids.filter { id: ID -> id.type.isUID() }.forEach { e: ID -> set.add(e) }
                }
            ) { node: Node<*> -> node.getType() === NodeType.DATA_ITEM }
        }
    }
}
