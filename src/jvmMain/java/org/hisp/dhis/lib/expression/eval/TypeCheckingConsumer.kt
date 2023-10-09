package org.hisp.dhis.lib.expression.eval

import org.hisp.dhis.lib.expression.ast.*
import org.hisp.dhis.lib.expression.spi.ExpressionData
import org.hisp.dhis.lib.expression.spi.Issues
import org.hisp.dhis.lib.expression.spi.ValueType
import java.util.stream.Collectors
import java.util.stream.IntStream

/**
 * Performs basic type checking based on the knowledge about the expectations of operators and functions as well as used
 * value literals.
 *
 * @author Jan Bernitt
 */
internal class TypeCheckingConsumer(private val issues: Issues) : NodeVisitor {

    override fun visitUnaryOperator(operator: Node<UnaryOperator>) {
        val operand = operator.child(0)
        val expected = operator.getValue().getValueType()
        val actual = operand.getValueType()
        if (!actual.isAssignableTo(expected)) {
            if (isStaticallyDefined(operand)) {
                checkEvaluateToType(expected, operand) {
                    String.format(
                        "Literal expression `%s` cannot be converted to type %s expected by operator `%s`",
                        Evaluate.normalise(operand), expected, operator.getRawValue())
                }
            }
            else {
                issues.addIssue(
                    actual.isMaybeAssignableTo(expected), operator, String.format(
                        "Incompatible operand type for unary operator `%s`, expected a %s but was: %s",
                        operator.getRawValue(), expected, actual))
            }
        }
    }

    override fun visitBinaryOperator(operator: Node<BinaryOperator>) {
        checkBinaryOperatorOperand(operator, operator.child(0), "left")
        checkBinaryOperatorOperand(operator, operator.child(1), "right")
    }

    private fun checkBinaryOperatorOperand(operator: Node<BinaryOperator>, operand: Node<*>, name: String) {
        val expected = operator.getValue().operandsType
        val leftActual = operand.getValueType()
        if (!leftActual.isAssignableTo(expected)) {
            if (isStaticallyDefined(operand)) {
                checkEvaluateToType(expected, operand) {
                    String.format(
                        "Literal expression `%s` cannot be converted to type %s expected by operator `%s`",
                        Evaluate.normalise(operand), expected, operator.getRawValue())
                }
            }
            else {
                issues.addIssue(
                    leftActual.isMaybeAssignableTo(expected), operator, String.format(
                        "Incompatible type for %s operand of binary operator `%s`, expected a %s but was: %s",
                        name, operator.getRawValue(), expected, leftActual))
            }
        }
    }

    override fun visitFunction(fn: Node<NamedFunction>) {
        checkArgumentTypesAreAssignable(fn, fn.getValue().parameterTypes)
        checkSameArgumentTypes(fn)
    }

    override fun visitModifier(modifier: Node<DataItemModifier>) {
        checkArgumentTypesAreAssignable(modifier, modifier.getValue().parameterTypes)
    }

    private fun checkSameArgumentTypes(fn: Node<NamedFunction>) {
        val f = fn.getValue()
        val expectedTypes = f.parameterTypes
        if (!expectedTypes.contains(ValueType.SAME) || expectedTypes.size == 1 && !f.isVarargs) {
            return  // has no SAME in the signature
        }
        var same: ValueType? = null
        for (i in 0 until fn.size()) {
            val expected = expectedTypes[Math.min(expectedTypes.size - 1, i)]
            if (expected.isSame()) {
                val arg = fn.child(i)
                val actual = arg.getValueType()
                if (same == null) {
                    same = actual
                }
                else if (actual !== same) {
                    val possiblySame = actual.isMaybeAssignableTo(same)
                    val indexes = IntStream.range(0, expectedTypes.size)
                        .filter { j: Int -> expectedTypes[j].isSame() }.mapToObj { j: Int -> (j + 1).toString() + "." }
                        .collect(Collectors.joining(" and "))
                    issues.addIssue(
                        possiblySame, fn, String.format(
                            "The argument types of parameters %s must be of the same type but were: %s, %s",
                            indexes, same, arg.getValueType()))
                    return
                }
            }
        }
    }

    private fun checkArgumentTypesAreAssignable(node: Node<*>?, expectedTypes: List<ValueType>) {
        for (i in 0 until node!!.size()) {
            val argument = node.child(i)
            val actual = argument.getValueType()
            val expected = if (i >= expectedTypes.size) expectedTypes[expectedTypes.size - 1] else expectedTypes[i]
            checkArgumentTypeIsAssignable(node, argument, expected, actual)
        }
    }

    private fun checkArgumentTypeIsAssignable(
        called: Node<*>?,
        argument: Node<*>,
        expected: ValueType,
        actual: ValueType
    ) {
        if (!actual.isAssignableTo(expected)) {
            val value = argument.getValue() as Int
            if (isStaticallyDefined(argument)) {
                checkEvaluateToType(expected, argument) {
                    String.format(
                        "Literal expression `%s` cannot be converted to type %s expected by function `%s`",
                        Evaluate.normalise(argument), expected, called!!.getRawValue())
                }
            }
            else {
                val possiblyAssignable = actual.isMaybeAssignableTo(expected)
                issues.addIssue(
                    possiblyAssignable, argument, String.format(
                        "Incompatible type for %d. argument of %s, expected %s but was: %s",
                        value + 1, called!!.getRawValue(), expected, actual))
            }
        }
    }

    private fun checkEvaluateToType(expected: ValueType, actual: Node<*>, errorMessage: () -> String) {
        try {
            evalTo(expected)(actual)
        } catch (ex: RuntimeException) {
            issues.addError(actual, errorMessage())
        }
    }

    private fun evalTo(expected: ValueType): (Node<*>) -> Unit {
        val eval = EvaluateFunction({_ -> null}, ExpressionData())
        return when (expected) {
            ValueType.STRING -> eval::evalToString
            ValueType.DATE -> eval::evalToDate
            ValueType.BOOLEAN -> eval::evalToBoolean
            ValueType.NUMBER -> eval::evalToNumber
            else -> { _ -> }
        }
    }

    companion object {
        /**
         * A statically defined node or expression can be computed to a deterministic result without any context.
         *
         * @return true, if the node is either a value literal or a composition of only operators, brackets and value
         * literals, false otherwise.
         */
        private fun isStaticallyDefined(node: Node<*>): Boolean {
            val type = node.getType()
            if (type.isValueLiteral()) return true
            if (type === NodeType.PAR || type === NodeType.ARGUMENT || type === NodeType.UNARY_OPERATOR)
                return isStaticallyDefined(node.child(0))
            return (type === NodeType.BINARY_OPERATOR)
                    && isStaticallyDefined(node.child(0))
                    && isStaticallyDefined(node.child(1))
        }
    }
}
