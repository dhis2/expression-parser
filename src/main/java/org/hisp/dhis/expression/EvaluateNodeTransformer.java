package org.hisp.dhis.expression;

import java.time.LocalDateTime;
import java.util.function.Function;

public class EvaluateNodeTransformer implements NodeTransformer<Object> {

    private <T> T eval(Node<?> node, Function<Object, T> cast) {
        return cast.apply(node.eval(this));
    }

    private Boolean castToBoolean(Node<?> node) {
        return eval(node, EvaluateNodeTransformer::castBoolean);
    }

    private Number castToNumber(Node<?> node) {
        return eval(node, EvaluateNodeTransformer::castNumber);
    }

    @Override
    public Object evalBinaryOperator(Node<BinaryOperator> operator) {
        Node<?> left = operator.child(0);
        Node<?> right = operator.child(1);
        switch (operator.getValue()) {
            case EQ: return BinaryOperator.equal(left.eval(this), right.eval(this));
            case NEQ: return BinaryOperator.notEqual(left.eval(this), right.eval(this));
            case AND: return BinaryOperator.and(left.eval(this), right.eval(this));
            case OR: return BinaryOperator.or(left.eval(this), right.eval(this));
            case LT: return BinaryOperator.lessThan(left.eval(this), right.eval(this));
            case LE: return BinaryOperator.lessThanOrEqual(left.eval(this), right.eval(this));
            case GT: return BinaryOperator.greaterThan(left.eval(this), right.eval(this));
            case GE: return BinaryOperator.greaterThanOrEqual(left.eval(this), right.eval(this));
            case ADD: return BinaryOperator.add(castToNumber(left), castToNumber(right));
            case SUB: return BinaryOperator.subtract(castToNumber(left), castToNumber(right));
            case MUL: return BinaryOperator.multiply(castToNumber(left), castToNumber(right));
            case DIV: return BinaryOperator.divide(castToNumber(left), castToNumber(right));
            case MOD: return BinaryOperator.modulo(castToNumber(left), castToNumber(right));
            case EXP: return BinaryOperator.exp(castToNumber(left), castToNumber(right));
            default: throw new UnsupportedOperationException();
        }
    }

    @Override
    public Object evalUnaryOperator(Node<UnaryOperator> operator) {
        Node<?> operand = operator.child(0);
        switch (operator.getValue()) {
            case NOT: return !castToBoolean(operand);
            case PLUS: return castToNumber(operand);
            case MINUS: return UnaryOperator.negate(castToNumber(operand));
            case DISTINCT: return operand.eval(this);
            default: throw new UnsupportedOperationException();
        }
    }

    @Override
    public Object evalFunction(Node<NamedFunction> function) {
        return null;
    }

    @Override
    public Object evalMethod(Node<NamedMethod> method) {
        return null;
    }

    @Override
    public Object evalDataValue(Node<DataValue> data) {
        return null;
    }

    @Override
    public Object evalNamedValue(Node<NamedValue> value) {
        return null;
    }

    @Override
    public Object evalNumber(Node<Double> value) {
        return value.getValue();
    }

    @Override
    public Object evalInteger(Node<Integer> value) {
        return value.getValue();
    }

    @Override
    public Object evalBoolean(Node<Boolean> value) {
        return value.getValue();
    }

    @Override
    public Object evalNull(Node<Void> value) {
        return null;
    }

    @Override
    public Object evalWildcard(Node<Void> value) {
        return null;
    }

    @Override
    public Object evalString(Node<String> value) {
        return value.getValue();
    }

    @Override
    public Object evalIdentifier(Node<?> value) {
        return value.getValue();
    }

    @Override
    public Object evalUid(Node<String> value) {
        return value.getValue();
    }

    @Override
    public Object evalDate(Node<LocalDateTime> value) {
        return value.getValue();
    }

    static Boolean castBoolean(Object value) {
        return (Boolean) value;
    }

    static Number castNumber(Object value) {
        return (Number) value;
    }

}
