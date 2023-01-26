package org.hisp.dhis.expression.eval;

import org.hisp.dhis.expression.ast.BinaryOperator;
import org.hisp.dhis.expression.ast.DataItemModifier;
import org.hisp.dhis.expression.ast.NamedFunction;
import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.ast.UnaryOperator;
import org.hisp.dhis.expression.ast.ValueType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.hisp.dhis.expression.eval.DescribeConsumer.toNormalisedExpression;

public class TypeCheckingConsumer implements NodeVisitor {

    static class Violation {
        final Node<?> node;
        final String msg;

        Violation(Node<?> node, String msg) {
            this.node = node;
            this.msg = msg;
        }

        @Override
        public String toString() {
            return msg+"\nin: "+ toNormalisedExpression(node);
        }
    }

    private final List<Violation> violations = new ArrayList<>();

    public List<Violation> getViolations() {
        return violations;
    }

    @Override
    public void visitUnaryOperator(Node<UnaryOperator> operator) {
        Node<?> operand = operator.child(0);
        ValueType expected = operator.getValue().getValueType();
        ValueType actual = operand.getValueType();
        if (!actual.isAssignableTo(expected)) {
            violations.add(new Violation(operator, format("Incompatible type for unary operator %s, expected %s but was: %s", operator.getValue().getSymbol(), expected, actual)));
        }
    }

    @Override
    public void visitBinaryOperator(Node<BinaryOperator> operator) {
        Node<?> left = operator.child(0);
        Node<?> right = operator.child(1);
        ValueType leftActual = left.getValueType();
        ValueType rightActual = right.getValueType();
        ValueType expected = operator.getValue().getOperandsType();
        boolean validIndividually = true;
        if (!leftActual.isAssignableTo(expected)) {
            validIndividually = false;
            violations.add(new Violation(operator, format("Incompatible type for left operand of binary operator %s, expected %s but was: %s", operator.getValue().getSymbol(), expected, leftActual)));
        }
        if (!rightActual.isAssignableTo(expected)) {
            validIndividually = false;
            violations.add(new Violation(operator, format("Incompatible type for right operand of binary operator %s, expected %s but was: %s", operator.getValue().getSymbol(), expected, rightActual)));
        }
        if (validIndividually && !ValueType.allSame(List.of(leftActual, rightActual))) {
            violations.add(new Violation(operator, format("The type of the left and right operand of binary operator %s must be same but were: %s, %s", operator.getValue().getSymbol(), leftActual, rightActual)));
        }
    }

    @Override
    public void visitFunction(Node<NamedFunction> function) {
        if (checkArgumentTypesAreAssignable(function, function.getValue().getParameterTypes()))
        {
            checkSameArgumentTypes(function);
        }
    }

    private void checkSameArgumentTypes(Node<NamedFunction> fn) {
        NamedFunction f = fn.getValue();
        List<ValueType> expectedTypes = f.getParameterTypes();
        if (!expectedTypes.contains(ValueType.SAME) || expectedTypes.size() == 1 && !f.isVarargs()) {
            return; // has no SAME in the signature
        }
        ValueType same = null;
        for (int i = 0; i < fn.size(); i++)
        {
            ValueType expected = expectedTypes.get(Math.min(expectedTypes.size()-1, i));
            if (expected.isSame()) {
                Node<?> arg = fn.child(i);
                ValueType actual = arg.getValueType();
                if (same == null) {
                    same = actual;
                } else if (actual != same) {
                    String s = IntStream.range(0, expectedTypes.size())
                            .filter(j -> expectedTypes.get(j).isSame()).mapToObj(j -> (j+1)+".").collect(joining(" and "));
                    violations.add(new Violation(fn, format("The argument types of parameters %s must be of the same type but were: %s, %s", s, same, arg.getValueType())));
                    return;
                }
            }
        }
    }

    private boolean checkArgumentTypesAreAssignable(Node<?> node, List<ValueType> expectedTypes) {
        boolean allAssignable = true;
        for (int i = 0; i < node.size(); i++)
        {
            Node<?> argument = node.child(i);
            ValueType actual = argument.getValueType();
            ValueType expected = i >= expectedTypes.size()
                    ? expectedTypes.get(expectedTypes.size()-1)
                    : expectedTypes.get(i);
            allAssignable &= checkArgumentTypeIsAssignable(node, argument, expected, actual);
        }
        return allAssignable;
    }

    @Override
    public void visitModifier(Node<DataItemModifier> modifier) {
        checkArgumentTypesAreAssignable(modifier, modifier.getValue().getParameterTypes());
    }

    private boolean checkArgumentTypeIsAssignable(Node<?> called, Node<?> argument, ValueType expected, ValueType actual) {
        if (!actual.isAssignableTo(expected)) {
            Integer value = (Integer) argument.getValue();
            violations.add(new Violation(called, format("Incompatible type for %d. argument, expected %s but was: %s", (value +1), expected, actual)));
            return false;
        }
        //TODO distinguish assign/coerce with error/warning
        return true;
    }

}
