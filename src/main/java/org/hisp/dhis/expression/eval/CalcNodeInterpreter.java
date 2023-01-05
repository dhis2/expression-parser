package org.hisp.dhis.expression.eval;

import lombok.RequiredArgsConstructor;
import org.hisp.dhis.expression.ast.BinaryOperator;
import org.hisp.dhis.expression.ast.DataItemModifier;
import org.hisp.dhis.expression.ast.NamedFunction;
import org.hisp.dhis.expression.ast.NamedValue;
import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.ast.NodeType;
import org.hisp.dhis.expression.ast.UnaryOperator;
import org.hisp.dhis.expression.ast.VariableType;
import org.hisp.dhis.expression.spi.DataItem;
import org.hisp.dhis.expression.spi.DataItemType;
import org.hisp.dhis.expression.spi.ExpressionBackend;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.hisp.dhis.expression.ast.NamedFunction.avg;

/**
 * A {@link NodeInterpreter} that calculates the expression result value
 * using a {@link ExpressionBackend} to implement the named functions, modifiers and data loading.
 *
 * @author Jan Bernitt
 */
@RequiredArgsConstructor
public class CalcNodeInterpreter implements NodeInterpreter<Object> {

    private final ExpressionBackend backend;
    private final Map<String, Object> programRuleVariableValues;
    private final Map<DataItem, Object> dataItemValues;
    private int dataItemIndex = 0;
    public CalcNodeInterpreter() {
        this(items -> null, Map.of(), Map.of());
    }

    @Override
    public Object evalBinaryOperator(Node<BinaryOperator> operator) {

        switch (operator.getValue()) {
            case EQ: return evalBinaryOperator(BinaryOperator::equal, operator, this::eval);
            case NEQ: return evalBinaryOperator(BinaryOperator::notEqual, operator, this::eval);
            case AND: return evalBinaryOperator(BinaryOperator::and, operator, this::eval);
            case OR: return evalBinaryOperator(BinaryOperator::or, operator, this::eval);
            case LT: return evalBinaryOperator(BinaryOperator::lessThan, operator, this::eval);
            case LE: return evalBinaryOperator(BinaryOperator::lessThanOrEqual, operator, this::eval);
            case GT: return evalBinaryOperator(BinaryOperator::greaterThan, operator, this::eval);
            case GE: return evalBinaryOperator(BinaryOperator::greaterThanOrEqual, operator, this::eval);
            case ADD: return evalBinaryOperator(BinaryOperator::add, operator, this::evalToNumber);
            case SUB: return evalBinaryOperator(BinaryOperator::subtract, operator, this::evalToNumber);
            case MUL: return evalBinaryOperator(BinaryOperator::multiply, operator, this::evalToNumber);
            case DIV: return evalBinaryOperator(BinaryOperator::divide, operator, this::evalToNumber);
            case MOD: return evalBinaryOperator(BinaryOperator::modulo, operator, this::evalToNumber);
            case EXP: return evalBinaryOperator(BinaryOperator::exp, operator, this::evalToNumber);
            default: throw new UnsupportedOperationException();
        }
    }

    private static <T> Object evalBinaryOperator(java.util.function.BinaryOperator<T> op, Node<?> operator, Function<Node<?>, T> eval) {
        Node<?> left = operator.child(0);
        Node<?> right = operator.child(1);
        T lVal = eval.apply(left);
        T rVal = eval.apply(right);
        return op.apply(lVal, rVal);
    }

    @Override
    public Object evalUnaryOperator(Node<UnaryOperator> operator) {
        Node<?> operand = operator.child(0);
        switch (operator.getValue()) {
            case NOT: return !evalToBoolean(operand);
            case PLUS: return evalToNumber(operand);
            case MINUS: return UnaryOperator.negate(evalToNumber(operand));
            default: throw new UnsupportedOperationException("Unary operator not supported for direct evaluation: "+operator.getValue());
        }
    }

    @Override
    public Object evalFunction(Node<NamedFunction> function) {
        if (function.getValue().isAggregating()) {
            return evalAggFunction(function);
        }
        switch (function.getValue()) {
            case firstNonNull: return backend.firstNonNull(function.children().map(node -> node.eval(this)).collect(toList()));
            case log: return function.size() == 1
                    ? backend.log(evalToNumber(function.child(0)))
                    : backend.log(evalToNumber(function.child(0))) / backend.log(evalToNumber(function.child(1)));
            case log10: return backend.log10(evalToNumber(function.child(0)));
            // "not implemented yet" => null
            default: return null;
        }
    }

    private Double evalAggFunction(Node<NamedFunction> function) {
        List<DataItem> items = function.aggregate(new ArrayList<>(), Node::toDataItem, List::add, node -> node.getType() == NodeType.DATA_ITEM);
        if (items.isEmpty()) throw new IllegalExpressionException("Aggregate function used without data item");
        double[] val0 = (double[]) dataItemValues.get(items.get(0));
        double[] values = new double[val0.length];
        for (dataItemIndex = 0; dataItemIndex < values.length; dataItemIndex++) {
            Number value = evalToNumber(function.child(0));
            values[dataItemIndex] = value == null ? Double.NaN : value.doubleValue();
        }
        dataItemIndex = 0;
        switch (function.getValue()) {
            case avg: return backend.avg(values);
            case count: return backend.count(values);
            case max: return backend.max(values);
            case median: return backend.median(values);
            case min: return backend.min(values);
            case percentileCont: return backend.percentileCont(values, evalToNumber(function.child(1)));
            case stddev: return backend.stddev(values);
            case stddevPop: return backend.stddevPop(values);
            case stddevSamp: return backend.stddevSamp(values);
            case sum: return backend.sum(values);
            case variance: return backend.variance(values);
            default: throw new UnsupportedOperationException();
        }
    }

    @Override
    public Void evalModifier(Node<DataItemModifier> modifier) {
        // modifiers do not have a return value
        // they only modify the evaluation context
        return null;
    }

    @Override
    public Object evalDataItem(Node<DataItemType> item) {
        Object value = dataItemValues.get(item.toDataItem());
        return value != null && value.getClass().isArray()
                ? Array.get(value, dataItemIndex)
                : value;
    }

    @Override
    public Object evalVariable(Node<VariableType> variable) {
        return programRuleVariableValues.get(evalToString(variable.child(0)));
    }

    @Override
    public Object evalNamedValue(Node<NamedValue> value) {
        return backend.namedValue(value.getValue());
    }

    @Override
    public Double evalNumber(Node<Double> value) {
        return value.getValue();
    }

    @Override
    public Integer evalInteger(Node<Integer> value) {
        return value.getValue();
    }

    @Override
    public Boolean evalBoolean(Node<Boolean> value) {
        return value.getValue();
    }

    @Override
    public Object evalNull(Node<Void> value) {
        return null;
    }

    @Override
    public String evalString(Node<String> value) {
        return value.getValue();
    }

    @Override
    public Object evalIdentifier(Node<?> value) {
        return value.getValue();
    }

    @Override
    public String evalUid(Node<String> value) {
        return value.getValue();
    }

    @Override
    public LocalDate evalDate(Node<LocalDate> value) {
        return value.getValue();
    }

    /*
    Result Type conversion
     */

    private <T> T eval(Node<?> node, Function<Object, T> cast) {
        return cast.apply(node.eval(this));
    }

    private String evalToString(Node<?> node) {
        return eval(node, String.class::cast);
    }

    private Boolean evalToBoolean(Node<?> node) {
        return eval(node, Boolean.class::cast);
    }

    private Number evalToNumber(Node<?> node) {
        return eval(node, Number.class::cast);
    }

    private Object eval(Node<?> node) {
        return node.eval(this);
    }
}
