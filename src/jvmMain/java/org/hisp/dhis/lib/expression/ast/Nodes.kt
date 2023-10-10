package org.hisp.dhis.lib.expression.ast

import org.hisp.dhis.lib.expression.spi.*
import org.hisp.dhis.lib.expression.ast.NodeAnnotations.Whitespace;
import java.time.LocalDate

/**
 * Implements the different [Node] types in the AST.
 *
 * @author Jan Bernitt
 */
object Nodes {

    abstract class AbstractNode<T> @JvmOverloads internal constructor(
        private val type: NodeType,
        private val rawValue: String,
        converter: (String) -> T,
        rethrowAs: (String, RuntimeException) -> RuntimeException? = { _: String, ex: RuntimeException -> ex }
    ) : Node<T> {
        private var value: T
        private var start: Position? = null
        private var end: Position? = null
        private var whitespace: Whitespace = Whitespace.DEFAULT

        init {
            try {
                value = converter(rawValue)
            } catch (ex: RuntimeException) {
                throw rethrowAs(rawValue, ex)!!
            }
        }

        override fun setStart(start: Position?) {
            this.start = start
        }

        override fun setEnd(end: Position?) {
            this.end = end
        }

        override fun setWhitespace(whitespace: Whitespace) {
            this.whitespace = whitespace
        }

        override fun getWhitespace(): Whitespace {
            return whitespace
        }

        override fun getStart(): Position? {
            return start
        }

        override fun getEnd(): Position? {
            return end
        }

        override fun getType(): NodeType {
            return type
        }

        override fun getRawValue(): String {
            return rawValue
        }

        override fun getValue(): T {
            return value
        }

        override fun visit(visitor: (Node<*>) -> Unit, filter: (Node<*>) -> Boolean) {
            if (filter(this)) {
                visitor(this)
            }
        }

        override fun toString(): String {
            val str = StringBuilder()
            toString(str, "")
            return str.toString()
        }

        open fun toString(str: StringBuilder, indent: String) {
            str.append(indent).append(javaClass.simpleName)
                .append('[').append(getType().name).append(" ").append(getRawValue()).append("]\n")
        }

        companion object {
            fun <E : Enum<E>?> rethrowAs(
                name: String?,
                values: Collection<E>,
                toText: (E) -> String
            ): (String, RuntimeException) -> RuntimeException {
                return { rawValue: String, _: RuntimeException ->
                    IllegalArgumentException(
                        String.format(
                            "Invalid %s option: '%s'%n\toptions are: %s",
                            name,
                            rawValue,
                            values.map(toText)
                        )
                    )
                }
            }
        }
    }

    abstract class ComplexNode<T> : AbstractNode<T> {

        private var children: MutableList<Node<*>> = ArrayList()

        internal constructor(type: NodeType, rawValue: String, converter: (String) -> T)
                : super(type, rawValue, converter)

        internal constructor(
            type: NodeType,
            rawValue: String,
            converter: (String) -> T,
            rethrowAs: (String, RuntimeException) -> RuntimeException
        ) : super(type, rawValue, converter, rethrowAs)

        override fun size(): Int {
            return children.size
        }

        override fun child(index: Int): Node<*> {
            return children[index]
        }

        override fun children(): Sequence<Node<*>> {
            return children.asSequence()
        }

        override fun addChild(child: Node<*>): Node<T> {
            children.add(child)
            return this
        }

        override fun transform(transformer: (Node<*>, List<Node<*>>) -> List<Node<*>>) {
            children = transformer(this, children).toMutableList()
            children.forEach { c: Node<*> -> c.transform(transformer) }
        }

        override fun visit(visitor: (Node<*>) -> Unit, filter: (Node<*>) -> Boolean) {
            super.visit(visitor, filter)
            children.forEach { child: Node<*> -> child.visit(visitor, filter) }
        }

        override fun toString(str: StringBuilder, indent: String) {
            super.toString(str, indent)
            for (c in children) (c as AbstractNode<*>).toString(str, "$indent  ")
        }
    }

    class ParenthesesNode(type: NodeType, rawValue: String) :
            ComplexNode<String>(type, rawValue, { it }) {
        override fun getValueType(): ValueType {
            return child(0).getValueType()
        }
    }

    class ArgumentNode(type: NodeType, rawValue: String) :
            ComplexNode<Int>(type, rawValue, Integer::valueOf) {
        override fun getValueType(): ValueType {
            return if (size() == 1) child(0).getValueType() else ValueType.MIXED
        }
    }

    class FunctionNode(type: NodeType, rawValue: String) :
            ComplexNode<NamedFunction>(
                type, rawValue, NamedFunction::fromName, rethrowAs(NamedFunction::class.simpleName,
                    NamedFunction.entries, NamedFunction::getName)) {
        override fun getValueType(): ValueType {
            val type = getValue().getValueType()
            return if (!type.isSame()) type else child(getValue().parameterTypes.indexOf(ValueType.SAME)).getValueType()
        }
    }

    class ModifierNode(type: NodeType, rawValue: String) :
            ComplexNode<DataItemModifier>(type, rawValue, DataItemModifier::valueOf) {
        override fun getValueType(): ValueType {
            return getValue().getValueType()
        }
    }

    abstract class ModifiedNode<T> : ComplexNode<T> {
        /**
         * This list of modifiers is always empty from pure parsing. It is used to aggregate the effective modifiers in
         * AST transformation step.
         */
        private val modifiers: MutableList<Node<*>> = ArrayList()

        internal constructor(type: NodeType, rawValue: String, converter: (String) -> T)
                : super(type, rawValue, converter)

        internal constructor(
            type: NodeType,
            rawValue: String,
            converter: (String) -> T,
            rethrowAs: (String, RuntimeException) -> RuntimeException
        ) : super(type, rawValue, converter, rethrowAs)

        override fun addModifier(modifier: Node<*>): Node<T> {
            modifiers.add(modifier)
            return this
        }

        override fun modifiers(): Iterable<Node<*>> {
            return modifiers
        }

        val queryModifiers: QueryModifiers
            get() {
                var mods = QueryModifiers()
                val sum = { a: Int?, b: Int -> if (a == null) b else a + b }
                for (mod in modifiers) {
                    val value = { mod.child(0).child(0).getValue() }
                    mods = when (mod.getValue() as DataItemModifier) {
                        DataItemModifier.aggregationType ->
                            mods.copy(aggregationType = value() as AggregationType)
                        DataItemModifier.maxDate ->
                            mods.copy(maxDate = value() as LocalDate)
                        DataItemModifier.minDate ->
                            mods.copy(minDate = value() as LocalDate)
                        DataItemModifier.periodOffset ->
                            mods.copy(periodOffset = sum(mods.periodOffset, value() as Int))
                        DataItemModifier.stageOffset ->
                            mods.copy(stageOffset = sum(mods.stageOffset, value() as Int))
                        DataItemModifier.yearToDate ->
                            mods.copy(yearToDate = true)
                        DataItemModifier.periodAggregation ->
                            mods.copy(periodAggregation = true)
                        DataItemModifier.subExpression ->
                            mods.copy(subExpression = value()as String)
                    }
                }
                return mods
            }
    }

    class DataItemNode(type: NodeType, rawValue: String) :
            ModifiedNode<DataItemType>(type, rawValue, DataItemType::fromSymbol, rethrowAs(
                DataItemType::class.simpleName, DataItemType.entries, DataItemType::getSymbol
            )) {
        /**
         * Just to cache the result
         */
        private var dataItemValue: DataItem? = null

        override fun getValueType(): ValueType {
            return ValueType.MIXED
        }

        override fun toDataItem(): DataItem {
            if (dataItemValue != null) return dataItemValue as DataItem
            val idGroups: MutableList<List<ID>> = ArrayList(listOf(listOf(), listOf(), listOf()))
            val itemType = getValue()
            for (i in 0 until size()) {
                val arg = child(i)
                val argC0 = arg.child(0)
                val type = if (argC0.getType() === NodeType.IDENTIFIER && argC0.getValue() is Tag) (argC0.getValue() as Tag).idType
                else itemType.getType(
                    size(),
                    i
                )
                var ids = arg.children()
                    .filter { n: Node<*> -> n.getType() === NodeType.UID }
                    .map { n: Node<*> -> ID(type, n.getRawValue()) }
                    .toList()
                if (ids.isEmpty() && arg.size() == 1 && arg.child(0).getType() === NodeType.IDENTIFIER) {
                    ids = listOf(ID(type, arg.child(0).getRawValue()))
                }
                idGroups[i] = ids
            }
            dataItemValue = DataItem(itemType, idGroups[0][0], idGroups[1], idGroups[2], queryModifiers)
            return dataItemValue!!
        }
    }

    abstract class SimpleNode<T> : AbstractNode<T> {
        internal constructor(type: NodeType, rawValue: String, converter: (String) -> T)
                : super(type, rawValue, converter)

        internal constructor(
            type: NodeType,
            rawValue: String,
            converter: (String) -> T,
            rethrowAs: (String, RuntimeException) -> RuntimeException
        ) : super(type, rawValue, converter, rethrowAs)

        override fun getValueType(): ValueType {
            return ValueType.STRING
        }
    }

    class TextNode(type: NodeType, rawValue: String) : SimpleNode<String>(type, rawValue, { it })

    class VariableNode(type: NodeType, rawValue: String) :
            ModifiedNode<VariableType>(type, rawValue, VariableType::fromSymbol) {
        /**
         * Might be provided after AST has been constructed to specify more precisely what type a variable has
         */
        private var actualValueType: ValueType? = null

        override fun toVariable(): Variable? {
            return if (getValue() !== VariableType.PROGRAM) {
                null
            }
            else Variable(child(0).getValue() as ProgramVariable, queryModifiers)
        }

        fun setActualValueType(actualValueType: ValueType?) {
            this.actualValueType = actualValueType
        }

        override fun getValueType(): ValueType {
            // the type of variable value is unknown statically
            return if (actualValueType == null) ValueType.MIXED else actualValueType!!
        }
    }

    class Utf8StringNode(type: NodeType, rawValue: String) :
            SimpleNode<String>(type, rawValue, { raw: String -> decode(raw) }) {
        companion object {
            fun decode(rawValue: String): String {
                if (rawValue.indexOf('\\') < 0) {
                    return rawValue // no special characters
                }
                val str = StringBuilder()
                val chars = rawValue.toCharArray()
                var i = 0
                while (i < chars.size) {
                    var c = chars[i]
                    if (c == '\\') {
                        c = chars[++i]
                        when (c) {
                            'u' -> {
                                str.appendCodePoint(
                                    String(charArrayOf(chars[++i], chars[++i], chars[++i], chars[++i])).toInt(16)
                                )
                            }
                            in '0'..'9' -> {
                                str.appendCodePoint(String(charArrayOf(chars[++i], chars[++i], chars[++i])).toInt(8))
                            }
                            else -> {
                                when (c) {
                                    'b' -> str.append('\b')
                                    't' -> str.append('\t')
                                    'n' -> str.append('\n')
                                    'f' -> str.append('\u000c')
                                    'r' -> str.append('\r')
                                    else -> str.append(c) // this is the escaped character
                                }
                            }
                        }
                    }
                    else {
                        str.append(c)
                    }
                    i++
                }
                return str.toString()
            }
        }
    }

    class UnaryOperatorNode(type: NodeType, rawValue: String) :
            ComplexNode<UnaryOperator>(type, rawValue, UnaryOperator::fromSymbol,
                rethrowAs(UnaryOperator::class.simpleName, UnaryOperator.entries, UnaryOperator::symbol)) {

                override fun getValueType(): ValueType {
            val type = getValue().getValueType()
            return if (type.isSame()) child(0).getValueType() else type
        }
    }

    class BinaryOperatorNode(type: NodeType, rawValue: String) : ComplexNode<BinaryOperator>(
        type, rawValue, BinaryOperator::fromSymbol,
        rethrowAs(BinaryOperator::class.simpleName, BinaryOperator.entries, BinaryOperator::symbol))
    {
        override fun getValueType(): ValueType {
            return getValue().getValueType()
        }
    }

    class BooleanNode(type: NodeType, rawValue: String) :
            SimpleNode<Boolean>(type, rawValue, String::toBoolean) {

        override fun getValueType(): ValueType {
            return ValueType.BOOLEAN
        }
    }

    class NumberNode(type: NodeType, rawValue: String) :
            SimpleNode<Double>(type, rawValue, String::toDouble) {

        override fun getValueType(): ValueType {
            return ValueType.NUMBER
        }
    }

    class IntegerNode(type: NodeType, rawValue: String) :
            SimpleNode<Int>(type, rawValue, String::toInt) {

        override fun getValueType(): ValueType {
            return ValueType.NUMBER
        }
    }

    class DateNode(type: NodeType, rawValue: String) :
            SimpleNode<LocalDate>(type, rawValue, LocalDate::parse) {

        override fun getValueType(): ValueType {
            return ValueType.DATE
        }
    }

    class ConstantNode(type: NodeType, rawValue: String) :
            SimpleNode<Unit>(type, rawValue, { _: String -> null }) {

        override fun getValueType(): ValueType {
            return ValueType.SAME
        }
    }

    class ReportingRateTypeNode(type: NodeType, rawValue: String) :
            SimpleNode<ReportingRateType>(
                type, rawValue, ReportingRateType::valueOf, rethrowAs(
                    ReportingRateType::class.simpleName, ReportingRateType.entries, ReportingRateType::name))

    class ProgramVariableNode(type: NodeType, rawValue: String) :
            SimpleNode<ProgramVariable>(type, rawValue, ProgramVariable::valueOf, rethrowAs(
                ProgramVariable::class.simpleName, ProgramVariable.entries, ProgramVariable::name))

    class NamedValueNode(type: NodeType, rawValue: String) :
            SimpleNode<NamedValue>(type, rawValue, NamedValue::valueOf, rethrowAs(
                NamedValue::class.simpleName, NamedValue.entries, NamedValue::name))

    class TagNode(type: NodeType, rawValue: String) : SimpleNode<Tag>(
        type, rawValue, Tag::valueOf, rethrowAs(Tag::class.simpleName, Tag.entries, Tag::name))

    class AggregationTypeNode(type: NodeType, rawValue: String) :
            SimpleNode<AggregationType>(type, rawValue, AggregationType::valueOf,
            rethrowAs(AggregationType::class.simpleName, AggregationType.entries, AggregationType::name))

    fun supplySubExpressionSQL(root: Node<*>) {
        root.transform { node: Node<*>, children: List<Node<*>> ->
            if (node.getValue() === NamedFunction.subExpression) {
                children.forEach { child: Node<*> ->
                    child.visit(NodeType.DATA_ITEM) { modified: Node<*> ->
                        modified
                            .addModifier(
                                ModifierNode(NodeType.MODIFIER, DataItemModifier.subExpression.name)
                                    .addChild(
                                        ArgumentNode(NodeType.ARGUMENT, "0")
                                            .addChild(
                                                TextNode(
                                                    NodeType.STRING,
                                                    "@" + System.identityHashCode(node)
                                                )
                                            )
                                    )
                            )
                    }
                }
            }
            children
        }
    }

    /**
     * Modifiers affect data items only. However, they can be applied to data items directly or indirectly. Within a
     * function or round bracket that has a modifier all data items within the bracket body are affected.
     *
     *
     * This transformation moves modifiers from being [Node.children] to be added as
     * [Node.addModifier].
     *
     *
     * This transformation should only be applied when the expression should be evaluated including resolving data items
     * to their actual value.
     *
     * @param root the node to start the transformation from.
     */
    fun propagateModifiers(root: Node<*>) {
        root.transform { node: Node<*>, children: List<Node<*>> ->
            val isModifier = { child: Node<*> -> child.getType() === NodeType.MODIFIER }
            if (node.getValue() is NamedFunction && (node.getValue() as NamedFunction).isAggregating()) {
                children.forEach { child: Node<*> ->
                    child.visit(NodeType.DATA_ITEM) { modified: Node<*> ->
                        modified.addModifier(
                            ModifierNode(NodeType.MODIFIER, DataItemModifier.periodAggregation.name)
                        )
                    }
                }
            }
            if (children.none(isModifier)) {
                return@transform children
            }
            // attach any modifier found on this level to any data item in the subtree of the child before them
            for (i in 1 until children.size) {
                val maybeModifier = children[i]
                if (maybeModifier.getType() === NodeType.MODIFIER) {
                    // go back 1 (or more if node before is a modifier)
                    var target = i - 1
                    while (target >= 0 && children[target].getType() === NodeType.MODIFIER) target--
                    if (target >= 0) {
                        val addModifier : (Node<*>) -> Unit = { modified: Node<*> -> modified.addModifier(maybeModifier) }
                        children[target].visit(NodeType.DATA_ITEM, addModifier)
                        children[target].visit(
                            addModifier
                        ) { n: Node<*> -> n.getType() === NodeType.VARIABLE && n.getValue() === VariableType.PROGRAM }
                    }
                }
            }
            children.filterNot(isModifier)
        }
    }
}
