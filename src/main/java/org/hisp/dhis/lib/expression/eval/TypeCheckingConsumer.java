package org.hisp.dhis.lib.expression.eval;

import lombok.RequiredArgsConstructor;
import org.hisp.dhis.lib.expression.ast.BinaryOperator;
import org.hisp.dhis.lib.expression.ast.DataItemModifier;
import org.hisp.dhis.lib.expression.ast.NamedFunction;
import org.hisp.dhis.lib.expression.ast.Node;
import org.hisp.dhis.lib.expression.ast.NodeType;
import org.hisp.dhis.lib.expression.ast.UnaryOperator;
import org.hisp.dhis.lib.expression.spi.Issues;
import org.hisp.dhis.lib.expression.spi.ValueType;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

/**
 * Performs basic type checking based on the knowledge about the expectations of operators and functions as well as used value literals.
 *
 * @author Jan Bernitt
 */
@RequiredArgsConstructor
final class TypeCheckingConsumer implements NodeVisitor {

    private final Issues issues;

    @Override
    public void visitUnaryOperator(Node<UnaryOperator> operator) {
        Node<?> operand = operator.child(0);
        ValueType expected = operator.getValue().getValueType();
        ValueType actual = operand.getValueType();
        if (!actual.isAssignableTo(expected)) {
            if (isStaticallyDefined(operand)) {
                checkEvaluateToType(expected, operand, () ->
                        format("Literal expression `%s` cannot be converted to type %s expected by operator `%s`",
                                Evaluate.normalise(operand), expected, operator.getRawValue()));
            } else {
                issues.addIssue(actual.isMaybeAssignableTo(expected), operator,
                        format("Incompatible operand type for unary operator `%s`, expected a %s but was: %s",
                                operator.getRawValue(), expected, actual));
            }
        }
    }

    @Override
    public void visitBinaryOperator(Node<BinaryOperator> operator) {
        checkBinaryOperatorOperand(operator, operator.child(0), "left");
        checkBinaryOperatorOperand(operator, operator.child(1), "right");
    }

    private void checkBinaryOperatorOperand(Node<BinaryOperator> operator, Node<?> operand, String name) {
        ValueType expected = operator.getValue().getOperandsType();
        ValueType leftActual = operand.getValueType();
        if (!leftActual.isAssignableTo(expected)) {
            if (isStaticallyDefined(operand)) {
                checkEvaluateToType(expected, operand, () ->
                        format("Literal expression `%s` cannot be converted to type %s expected by operator `%s`",
                                Evaluate.normalise(operand), expected, operator.getRawValue()));
            } else {
                issues.addIssue(leftActual.isMaybeAssignableTo(expected), operator,
                        format("Incompatible type for %s operand of binary operator `%s`, expected a %s but was: %s",
                                name, operator.getRawValue(), expected, leftActual));
            }
        }
    }

    @Override
    public void visitFunction(Node<NamedFunction> function) {
        checkArgumentTypesAreAssignable(function, function.getValue().getParameterTypes());
        checkSameArgumentTypes(function);
    }

    @Override
    public void visitModifier(Node<DataItemModifier> modifier) {
        checkArgumentTypesAreAssignable(modifier, modifier.getValue().getParameterTypes());
    }

    private void checkSameArgumentTypes(Node<NamedFunction> fn) {
        NamedFunction f = fn.getValue();
        List<ValueType> expectedTypes = f.getParameterTypes();
        if (!expectedTypes.contains(ValueType.SAME) || expectedTypes.size() == 1 && !f.isVarargs()) {
            return; // has no SAME in the signature
        }
        ValueType same = null;
        for (int i = 0; i < fn.size(); i++) {
            ValueType expected = expectedTypes.get(Math.min(expectedTypes.size() - 1, i));
            if (expected.isSame()) {
                Node<?> arg = fn.child(i);
                ValueType actual = arg.getValueType();
                if (same == null) {
                    same = actual;
                } else if (actual != same) {
                    boolean possiblySame = actual.isMaybeAssignableTo(same);
                    String indexes = IntStream.range(0, expectedTypes.size())
                            .filter(j -> expectedTypes.get(j).isSame()).mapToObj(j -> (j + 1) + ".")
                            .collect(joining(" and "));
                    issues.addIssue(possiblySame, fn,
                            format("The argument types of parameters %s must be of the same type but were: %s, %s",
                                    indexes, same, arg.getValueType()));
                    return;
                }
            }
        }
    }

    private void checkArgumentTypesAreAssignable(Node<?> node, List<ValueType> expectedTypes) {
        for (int i = 0; i < node.size(); i++) {
            Node<?> argument = node.child(i);
            ValueType actual = argument.getValueType();
            ValueType expected = i >= expectedTypes.size()
                    ? expectedTypes.get(expectedTypes.size() - 1)
                    : expectedTypes.get(i);
            checkArgumentTypeIsAssignable(node, argument, expected, actual);
        }
    }

    private void checkArgumentTypeIsAssignable(Node<?> called, Node<?> argument, ValueType expected, ValueType actual) {
        if (!actual.isAssignableTo(expected)) {
            Integer value = (Integer) argument.getValue();
            if (isStaticallyDefined(argument)) {
                checkEvaluateToType(expected, argument, () ->
                        format("Literal expression `%s` cannot be converted to type %s expected by function `%s`",
                                Evaluate.normalise(argument), expected, called.getRawValue()));
            } else {
                boolean possiblyAssignable = actual.isMaybeAssignableTo(expected);
                issues.addIssue(possiblyAssignable, argument,
                        format("Incompatible type for %d. argument of %s, expected %s but was: %s",
                                (value + 1), called.getRawValue(), expected, actual));
            }
        }
    }

    private void checkEvaluateToType(ValueType expected, Node<?> actual, Supplier<String> errorMessage) {
        try {
            evalTo(expected).accept(actual);
        } catch (RuntimeException ex) {
            issues.addError(actual, errorMessage.get());
        }
    }

    private Consumer<Node<?>> evalTo(ValueType expected) {
        EvaluateFunction eval = new EvaluateFunction(null, null);
        switch (expected) {
            case STRING:
                return eval::evalToString;
            case DATE:
                return eval::evalToDate;
            case BOOLEAN:
                return eval::evalToBoolean;
            case NUMBER:
                return eval::evalToNumber;
            default:
                return node -> {
                }; // we can't tell
        }
    }

    /**
     * A statically defined node or expression can be computed to a deterministic result without any context.
     *
     * @return true, if the node is either a value literal or a composition of only operators, brackets and value literals, false otherwise.
     */
    private static boolean isStaticallyDefined(Node<?> node) {
        NodeType type = node.getType();
        if (type.isValueLiteral()) return true;
        if (type == NodeType.PAR || type == NodeType.ARGUMENT || type == NodeType.UNARY_OPERATOR)
            return isStaticallyDefined(node.child(0));
        if (type == NodeType.BINARY_OPERATOR)
            return isStaticallyDefined(node.child(0)) && isStaticallyDefined(node.child(1));
        return false;
    }
}
