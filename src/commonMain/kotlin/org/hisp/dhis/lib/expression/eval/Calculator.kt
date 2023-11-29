package org.hisp.dhis.lib.expression.eval

import kotlinx.datetime.LocalDate
import org.hisp.dhis.lib.expression.ast.*
import org.hisp.dhis.lib.expression.ast.UnaryOperator.Companion.negate
import org.hisp.dhis.lib.expression.spi.*

/**
 * A [NodeInterpreter] that calculates the expression result value using a [ExpressionFunctions] to
 * implement the named functions, modifiers and data loading.
 *
 * @author Jan Bernitt
 */
internal class Calculator(
    private val functions: ExpressionFunctions,
    private val data: ExpressionData
) : NodeInterpreter<Any?> {

    private var dataItemIndex = 0

    override fun evalBinaryOperator(operator: Node<BinaryOperator>): Any? {
        return when (operator.getValue()) {
            BinaryOperator.EQ -> evalBinaryLogicOperator(BinaryOperator::equal, operator, this::evalToMixed)
            BinaryOperator.NEQ -> evalBinaryLogicOperator(BinaryOperator::notEqual, operator, this::evalToMixed)
            BinaryOperator.AND -> evalBinaryOperator(BinaryOperator::and, operator, this::evalToBoolean)
            BinaryOperator.OR -> evalBinaryOperator(BinaryOperator::or, operator, this::evalToBoolean)
            BinaryOperator.LT -> evalBinaryLogicOperator(BinaryOperator::lessThan, operator, this::evalToMixed)
            BinaryOperator.LE -> evalBinaryLogicOperator(BinaryOperator::lessThanOrEqual, operator, this::evalToMixed)
            BinaryOperator.GT -> evalBinaryLogicOperator(BinaryOperator::greaterThan, operator, this::evalToMixed)
            BinaryOperator.GE -> evalBinaryLogicOperator(BinaryOperator::greaterThanOrEqual, operator, this::evalToMixed)
            BinaryOperator.ADD -> evalBinaryOperator(BinaryOperator::add, operator, this::evalToNumber)
            BinaryOperator.SUB -> evalBinaryOperator(BinaryOperator::subtract, operator, this::evalToNumber)
            BinaryOperator.MUL -> evalBinaryOperator(BinaryOperator::multiply, operator, this::evalToNumber)
            BinaryOperator.DIV -> evalBinaryOperator(BinaryOperator::divide, operator, this::evalToNumber)
            BinaryOperator.MOD -> evalBinaryOperator(BinaryOperator::modulo, operator, this::evalToNumber)
            BinaryOperator.EXP -> evalBinaryOperator(BinaryOperator::exp, operator, this::evalToNumber)
        }
    }

    override fun evalUnaryOperator(operator: Node<UnaryOperator>): Any {
        val operand = operator.child(0)
        return when (operator.getValue()) {
            UnaryOperator.NOT -> evalToBoolean(operand)?.not()
            UnaryOperator.PLUS -> evalToNumber(operand)
            UnaryOperator.MINUS -> negate(evalToNumber(operand))
            else -> throw IllegalExpressionException("Unary operator not supported for direct evaluation: " + operator.getValue())
        }!!
    }

    override fun evalFunction(fn: Node<NamedFunction>): Any? {
        val fnInfo = fn.getValue()
        if (fnInfo.isAggregating()) {
            return evalAggFunction(fn)
        }
        return if (fnInfo === NamedFunction.subExpression) {
            null // return value of only data item in the expression from map
        }
        else when (fnInfo) {
            NamedFunction.firstNonNull -> functions.firstNonNull(evalToMixed(fn.children()))
            NamedFunction.greatest -> functions.greatest(evalToNumbers(fn.children()))
            NamedFunction.ifThenElse -> functions.ifThenElse(
                evalToBoolean(fn.child(0)),
                evalToMixed(fn.child(1)),
                evalToMixed(fn.child(2)))
            NamedFunction.isNotNull -> functions.isNotNull(evalToMixed(fn.child(0)))
            NamedFunction.isNull -> functions.isNull(evalToMixed(fn.child(0)))
            NamedFunction.least -> functions.least(evalToNumbers(fn.children()))
            NamedFunction.log -> if (fn.size() == 1) functions.log(evalToNumber(fn.child(0)))
            else functions.log(evalToNumber(fn.child(0))) / functions.log(evalToNumber(fn.child(1)))
            NamedFunction.log10 -> functions.log10(evalToNumber(fn.child(0)))
            NamedFunction.removeZeros -> functions.removeZeros(evalToNumber(fn.child(0)))
            NamedFunction.d2_addDays -> functions.d2_addDays(
                evalToDate(fn.child(0)),
                evalToNumber(fn.child(1)))
            NamedFunction.d2_ceil -> functions.d2_ceil(evalToNumber(fn.child(0)))
            NamedFunction.d2_concatenate -> functions.d2_concatenate(evalToStrings(fn.children()))
            NamedFunction.d2_count -> functions.d2_count(evalToVar(fn.child(0)))
            NamedFunction.d2_countIfValue -> functions.d2_countIfValue(
                evalToVar(fn.child(0)),
                evalToString(fn.child(1)))
            NamedFunction.d2_countIfZeroPos -> functions.d2_countIfZeroPos(evalToVar(fn.child(0)))
            NamedFunction.d2_daysBetween -> functions.d2_daysBetween(
                evalToDate(fn.child(0)),
                evalToDate(fn.child(1)))
            NamedFunction.d2_extractDataMatrixValue -> functions.d2_extractDataMatrixValue(
                evalToString(fn.child(0)),
                evalToString(fn.child(1)))
            NamedFunction.d2_floor -> functions.d2_floor(evalToNumber(fn.child(0)))
            NamedFunction.d2_hasUserRole -> functions.d2_hasUserRole(
                evalToString(fn.child(0)),
                data.supplementaryValues["USER"])
            NamedFunction.d2_hasValue -> functions.d2_hasValue(evalToVar(fn.child(0)))
            NamedFunction.d2_inOrgUnitGroup -> functions.d2_inOrgUnitGroup(
                evalToString(fn.child(0)),
                data.programRuleVariableValues["org_unit"],
                data.supplementaryValues)
            NamedFunction.d2_lastEventDate -> functions.d2_lastEventDate(
                evalToVar(fn.child(0)))
            NamedFunction.d2_left -> functions.d2_left(
                evalToString(fn.child(0)),
                evalToInteger(fn.child(1)))
            NamedFunction.d2_length -> functions.d2_length(evalToString(fn.child(0)))
            NamedFunction.d2_maxValue -> functions.d2_maxValue(evalToVar(fn.child(0)))
            NamedFunction.d2_minutesBetween -> functions.d2_minutesBetween(
                evalToDate(fn.child(0)),
                evalToDate(fn.child(1)))
            NamedFunction.d2_minValue -> functions.d2_minValue(evalToVar(fn.child(0)))
            NamedFunction.d2_modulus -> functions.d2_modulus(
                evalToNumber(fn.child(0)),
                evalToNumber(fn.child(1)))
            NamedFunction.d2_monthsBetween -> functions.d2_monthsBetween(
                evalToDate(fn.child(0)),
                evalToDate(fn.child(1)))
            NamedFunction.d2_oizp -> functions.d2_oizp(evalToNumber(fn.child(0)))
            NamedFunction.d2_right -> functions.d2_right(
                evalToString(fn.child(0)),
                evalToInteger(fn.child(1)))
            NamedFunction.d2_round -> functions.d2_round(
                evalToNumber(fn.child(0)), if (fn.size() <= 1) 0
                else evalToInteger(fn.child(1)))
            NamedFunction.d2_split -> functions.d2_split(
                evalToString(fn.child(0)),
                evalToString(fn.child(1)),
                evalToInteger(fn.child(2)))
            NamedFunction.d2_substring -> functions.d2_substring(
                evalToString(fn.child(0)),
                evalToInteger(fn.child(1)),
                evalToInteger(fn.child(2)))
            NamedFunction.d2_validatePattern -> functions.d2_validatePattern(
                evalToString(fn.child(0)),
                evalToString(fn.child(1)))
            NamedFunction.d2_weeksBetween -> functions.d2_weeksBetween(
                evalToDate(fn.child(0)),
                evalToDate(fn.child(1)))
            NamedFunction.d2_yearsBetween -> functions.d2_yearsBetween(
                evalToDate(fn.child(0)),
                evalToDate(fn.child(1)))
            NamedFunction.d2_zing -> functions.d2_zing(evalToNumber(fn.child(0)))
            NamedFunction.d2_zpvc -> functions.d2_zpvc(evalToNumbers(fn.children()))
            NamedFunction.d2_zScoreHFA -> functions.d2_zScoreHFA(
                evalToNumber(fn.child(0)),
                evalToNumber(fn.child(1)),
                evalToString(fn.child(2)))
            NamedFunction.d2_zScoreWFA -> functions.d2_zScoreWFA(
                evalToNumber(fn.child(0)),
                evalToNumber(fn.child(1)),
                evalToString(fn.child(2)))
            NamedFunction.d2_zScoreWFH -> functions.d2_zScoreWFH(
                evalToNumber(fn.child(0)),
                evalToNumber(fn.child(1)),
                evalToString(fn.child(2)))
            NamedFunction.normDistCum -> functions.normDistCum(
                evalToNumber(fn.child(0)),
                if (fn.size() > 1) evalToNumber(fn.child(1))
                else
                    evalAggFunction(Nodes.FunctionNode(NodeType.FUNCTION, "avg").addChild(fn.child(0))),
                if (fn.size() > 2) evalToNumber(fn.child(2))
                else
                    evalAggFunction(Nodes.FunctionNode(NodeType.FUNCTION, "stddev").addChild(fn.child(0))))
            NamedFunction.normDistDen -> functions.normDistDen(
                evalToNumber(fn.child(0)),
                if (fn.size() > 1) evalToNumber(fn.child(1))
                else
                    evalAggFunction(Nodes.FunctionNode(NodeType.FUNCTION, "avg").addChild(fn.child(0))),
                if (fn.size() > 2) evalToNumber(fn.child(2))
                else
                    evalAggFunction(Nodes.FunctionNode(NodeType.FUNCTION, "stddev").addChild(fn.child(0))))
            else -> functions.unsupported(fnInfo.getName())
        }
    }

    private fun evalAggFunction(fn: Node<NamedFunction>): Double? {
        val items: MutableList<DataItem> = mutableListOf()
        fn.child(0).visit(NodeType.DATA_ITEM) { node: Node<*> ->
            run {
                val item = node.toDataItem()
                if (item != null)
                    items.add(item)
            }
        }
        if (items.isEmpty()) throw IllegalExpressionException("Aggregate function used without data item")
        val vector = data.dataItemValues[items[0]] as DoubleArray?
            ?: throw IllegalExpressionException("Aggregate function used with undefined data item")
        val values = DoubleArray(vector.size)
        dataItemIndex = 0
        while (dataItemIndex < values.size) {
            val value: Number? = evalToNumber(fn.child(0))
            values[dataItemIndex] = value?.toDouble() ?: Double.NaN
            dataItemIndex++
        }
        dataItemIndex = 0
        return when (fn.getValue()) {
            NamedFunction.avg -> functions.avg(values)
            NamedFunction.count -> functions.count(values)
            NamedFunction.max -> functions.max(values)
            NamedFunction.median -> functions.median(values)
            NamedFunction.min -> functions.min(values)
            NamedFunction.percentileCont -> functions.percentileCont(values, evalToNumber(fn.child(1)))
            NamedFunction.stddev -> functions.stddev(values)
            NamedFunction.stddevPop -> functions.stddevPop(values)
            NamedFunction.stddevSamp -> functions.stddevSamp(values)
            NamedFunction.sum -> functions.sum(values)
            NamedFunction.variance -> functions.variance(values)
            else -> throw UnsupportedOperationException()
        }
    }

    override fun evalModifier(modifier: Node<DataItemModifier>): Unit {
        // modifiers do not have a return value
        // they only modify the evaluation context
        return
    }

    override fun evalDataItem(item: Node<DataItemType>): Any? {
        val dataItem = item.toDataItem()
        if (data.programRuleVariableValues.isNotEmpty()) {
            return if (dataItem == null) null else data.programRuleVariableValues[dataItem.getKey()]
        }
        return when (val value = data.dataItemValues[dataItem]) {
            is Array<*> -> value[dataItemIndex]
            is DoubleArray -> value[dataItemIndex]
            else -> value
        }
    }

    override fun evalVariable(variable: Node<VariableType>): Any? {
        val name = evalToString(variable.child(0))
        val values =
            data.programRuleVariableValues.ifEmpty { data.programVariableValues }
        if (!values.containsKey(name)) throw IllegalExpressionException("Unknown variable: '$name'")
        return values[name]
    }

    override fun evalNamedValue(value: Node<NamedValue>): Any? {
        return data.namedValues[value.getRawValue()]
    }

    override fun evalNumber(value: Node<Double>): Double {
        return value.getValue()
    }

    override fun evalInteger(value: Node<Int>): Int {
        return value.getValue()
    }

    override fun evalBoolean(value: Node<Boolean>): Boolean {
        return value.getValue()
    }

    override fun evalNull(value: Node<Unit>): Any? {
        return null
    }

    override fun evalString(value: Node<String>): String {
        return value.getValue()
    }

    override fun evalIdentifier(value: Node<Any>): Any {
        return value.getValue()
    }

    override fun evalUid(value: Node<String>): String {
        return value.getValue()
    }

    override fun evalDate(value: Node<LocalDate>): LocalDate {
        return value.getValue()
    }

    /*
    Result Type conversion
     */
    private fun <T> eval(node: Node<*>, to: String, coerce: (Any?) -> T?): T? {
        var value: Any? = null
        return try {
            value = node.eval(this::evalNode)
            coerce(value)
        } catch (ex: IllegalExpressionException) {
            throw ex
        } catch (ex: UnsupportedOperationException) {
            throw ex
        } catch (ex: RuntimeException) {
            val type = if (value == null) "" else value::class.simpleName
            val expr = Describer.toNormalisedExpression(node)
            throw IllegalExpressionException("Failed to coerce value '$value' ($type) to $to in expression: $expr")
        }
    }

    fun evalToString(node: Node<*>): String? {
        return eval(node, "String", Typed::toStringTypeCoercion)
    }

    fun evalToBoolean(node: Node<*>): Boolean? {
        return eval(node, "Boolean", Typed::toBooleanTypeCoercion)
    }

    fun evalToNumber(node: Node<*>): Double? {
        return eval(node, "Double", Typed::toNumberTypeCoercion)
    }

    fun evalToDate(node: Node<*>): LocalDate? {
        return eval(node, "Date", Typed::toDateTypeCoercion)
    }

    private fun evalToInteger(node: Node<*>): Int? {
        val num = evalToNumber(node) ?: return null
        require(num % 1.0 == 0.0) { "Expected an integer but got a floating point for: $node" }
        return num.toInt()
    }

    private fun evalToMixed(node: Node<*>): Any? {
        return eval(node, "Any", Typed::toMixedTypeTypeCoercion)
    }

    private fun evalToVar(node: Node<*>): VariableValue? {
        return eval(node, "Variable") { v: Any? -> if (v is String) data.programRuleVariableValues[v] else v as VariableValue? }
    }

    private fun evalToMixed(nodes: Sequence<Node<*>>): List<*> {
        return evalToList(nodes) { node: Node<*> -> this.evalToMixed(node) }
    }

    private fun evalToNumbers(nodes: Sequence<Node<*>>): List<Double?> {
        return evalToList(nodes) { node: Node<*> -> evalToNumber(node) }
    }

    private fun evalToStrings(nodes: Sequence<Node<*>>): List<String?> {
        return evalToList(nodes) { node: Node<*> -> evalToString(node) }
    }

    companion object {
        private fun <T> evalBinaryLogicOperator(op: (T?, T?) -> Boolean, operator: Node<*>, eval: (Node<*>) -> T?): Boolean {
            val left = operator.child(0)
            val right = operator.child(1)
            val lVal = eval(left)
            val rVal = eval(right)
            return op(lVal, rVal)
        }

        private fun <T> evalBinaryOperator(op: (T?, T?) -> T?, operator: Node<*>, eval: (Node<*>) -> T?): T? {
            val left = operator.child(0)
            val right = operator.child(1)
            val lVal = eval(left)
            val rVal = eval(right)
            return op(lVal, rVal)
        }

        private fun <T> evalToList(nodes: Sequence<Node<*>>, map: (Node<*>) -> T?): List<T?> {
            return nodes.map(map).toList()
        }
    }
}
