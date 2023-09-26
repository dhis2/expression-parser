package org.hisp.dhis.lib.expression.eval

import org.hisp.dhis.lib.expression.ast.*
import org.hisp.dhis.lib.expression.spi.DataItem
import org.hisp.dhis.lib.expression.spi.DataItemType
import java.time.LocalDate

/**
 * Converts an AST back into a "normalised" or substituted [String] form.
 *
 * @author Jan Bernitt
 */
internal class DescribeConsumer(
    private val dataItemValues: Map<DataItem, Number>,
    private val displayNames: Map<String, String>
) : NodeVisitor {
    private val out = StringBuilder()

    /**
     * After parsing the root is always a parenthesis that should not be printed. To remember that this was skipped this
     * flag is used because the expression could also start with a `(` without that being the root itself.
     */
    private var wasRoot = false
    override fun toString(): String {
        return out.toString()
    }

    override fun visitParentheses(group: Node<Unit>) {
        val isRoot = !wasRoot && out.toString().isEmpty()
        if (isRoot) wasRoot = true
        out.append(group.getWhitespace()!!.before)
        if (!isRoot) out.append('(')
        group.walkChildren(this, null)
        if (!isRoot) out.append(')')
        out.append(group.getWhitespace()!!.after)
    }

    override fun visitArgument(argument: Node<Int>) {
        out.append(argument.getWhitespace()!!.before)
        argument.walkChildren(
            this
        ) { c1: Node<*>, c2: Node<*> -> out.append(if (c1.getType() === NodeType.UID && c2.getType() === NodeType.UID) "&" else "") }
        out.append(argument.getWhitespace()!!.after)
    }

    override fun visitBinaryOperator(operator: Node<BinaryOperator>) {
        operator.child(0).walk(this)
        out.append(operator.getWhitespace()!!.before(" "))
        out.append(operator.getRawValue())
        out.append(operator.getWhitespace()!!.after(" "))
        operator.child(1).walk(this)
    }

    override fun visitUnaryOperator(operator: Node<UnaryOperator>) {
        val rawValue: String = operator.getRawValue()
        val isWord = Character.isLetter(rawValue[0])
        val ifDefault = if (isWord) " " else ""
        out.append(operator.getWhitespace()!!.before(ifDefault))
        out.append(rawValue)
        out.append(operator.getWhitespace()!!.after(ifDefault))
        operator.child(0).walk(this)
    }

    override fun visitFunction(fn: Node<NamedFunction>) {
        out.append(fn.getWhitespace()!!.before)
        out.append(fn.getValue().getName()).append('(')
        fn.walkChildren(this) { _, _ -> out.append(',') }
        out.append(')')
        out.append(fn.getWhitespace()!!.after)
    }

    override fun visitModifier(modifier: Node<DataItemModifier>) {
        out.append(modifier.getWhitespace()!!.before)
        out.append('.').append(modifier.getValue().name).append('(')
        modifier.walkChildren(this) { _, _ -> out.append(',') }
        out.append(')')
        out.append(modifier.getWhitespace()!!.after)
    }

    override fun visitDataItem(item: Node<DataItemType>) {
        val value = dataItemValues[item.toDataItem()]
        out.append(item.getWhitespace()!!.before)
        if (value != null) {
            out.append(value)
        }
        else {
            describeDataItem(item)
        }
        out.append(item.getWhitespace()!!.after)
    }

    private fun describeDataItem(item: Node<DataItemType>) {
        val c0 = item.child(0)
        val isEventDate = c0.child(0).getValue() === Tag.PS_EVENTDATE
        if (!isEventDate) {
            out.append(item.getValue().symbol)
            out.append('{')
        }
        for (i in 0 until item.size()) {
            if (i > 0) out.append('.')
            item.child(i).walk(this)
        }
        if (!isEventDate) {
            out.append('}')
        }
        visitModifiers(item)
    }

    private fun visitModifiers(item: Node<*>) {
        for (mod in item.modifiers()) {
            if (mod.getValue() !== DataItemModifier.periodAggregation) {
                mod.walk(this)
            }
        }
    }

    override fun visitVariable(variable: Node<VariableType>) {
        out.append(variable.getWhitespace()!!.before)
        if (displayNames.isNotEmpty()) {
            val name = displayNames[variable.child(0).getRawValue()]
            if (name != null) {
                out.append(name)
                out.append(variable.getWhitespace()!!.after)
                return
            }
        }
        val symbol: String = variable.getRawValue()
        val hasSymbol = symbol.isNotEmpty()
        if (hasSymbol) {
            out.append(symbol)
            out.append('{')
        }
        variable.child(0).walk(this)
        if (hasSymbol) {
            out.append('}')
        }
        visitModifiers(variable)
        out.append(variable.getWhitespace()!!.after)
    }

    override fun visitNamedValue(value: Node<NamedValue>) {
        appendValue(value, "[" + value.getRawValue() + "]")
    }

    override fun visitNumber(value: Node<Double>) {
        appendValue(value, value.getRawValue())
    }

    override fun visitInteger(value: Node<Int>) {
        appendValue(value, value.getRawValue())
    }

    override fun visitBoolean(value: Node<Boolean>) {
        appendValue(value, value.getRawValue())
    }

    override fun visitNull(value: Node<Unit>) {
        appendValue(value, "null")
    }

    override fun visitString(value: Node<String>) {
        appendValue(value, "'" + value.getRawValue() + "'")
    }

    override fun visitIdentifier(value: Node<*>) {
        appendValue(value, value.getRawValue() + if (value.getValue() is Tag) ":" else "")
    }

    override fun visitUid(value: Node<String>) {
        val uid: String = value.getValue()
        appendValue(value, displayNames[uid] ?: uid)
    }

    override fun visitDate(value: Node<LocalDate>) {
        appendValue(value, value.getRawValue())
    }

    private fun appendValue(node: Node<*>, value: String) {
        out.append(node.getWhitespace()!!.before)
        out.append(value)
        out.append(node.getWhitespace()!!.after)
    }

    companion object {
        fun toNormalisedExpression(root: Node<*>): String {
            return toValueExpression(root, java.util.Map.of())
        }

        fun toValueExpression(root: Node<*>, dataItemValues: Map<DataItem, Number>): String {
            val walker = DescribeConsumer(dataItemValues, java.util.Map.of())
            root.walk(walker)
            return walker.toString()
        }

        fun toDisplayExpression(root: Node<*>, displayNames: Map<String, String>): String {
            val walker = DescribeConsumer(java.util.Map.of(), displayNames)
            root.walk(walker)
            return walker.toString()
        }
    }
}
