package org.hisp.dhis.expression.eval;

import lombok.RequiredArgsConstructor;
import org.hisp.dhis.expression.ast.BinaryOperator;
import org.hisp.dhis.expression.ast.DataItemModifier;
import org.hisp.dhis.expression.ast.NamedFunction;
import org.hisp.dhis.expression.ast.NamedValue;
import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.ast.NodeType;
import org.hisp.dhis.expression.ast.Typed;
import org.hisp.dhis.expression.ast.UnaryOperator;
import org.hisp.dhis.expression.ast.VariableType;
import org.hisp.dhis.expression.spi.DataItem;
import org.hisp.dhis.expression.spi.DataItemType;
import org.hisp.dhis.expression.spi.ExpressionFunctions;
import org.hisp.dhis.expression.spi.IllegalExpressionException;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * A {@link NodeInterpreter} that calculates the expression result value
 * using a {@link ExpressionFunctions} to implement the named functions, modifiers and data loading.
 *
 * @author Jan Bernitt
 */
@RequiredArgsConstructor
public class EvaluateFunction implements NodeInterpreter<Object> {

    private final ExpressionFunctions functions;
    private final Map<String, Object> programRuleVariableValues;
    private final Map<DataItem, Object> dataItemValues;
    private int dataItemIndex = 0;
    public EvaluateFunction() {
        this(items -> null, Map.of(), Map.of());
    }

    @Override
    public Object evalBinaryOperator(Node<BinaryOperator> operator) {

        switch (operator.getValue()) {
            case EQ: return evalBinaryOperator(BinaryOperator::equal, operator, this::evalToObj);
            case NEQ: return evalBinaryOperator(BinaryOperator::notEqual, operator, this::evalToObj);
            case AND: return evalBinaryOperator(BinaryOperator::and, operator, this::evalToBoolean);
            case OR: return evalBinaryOperator(BinaryOperator::or, operator, this::evalToBoolean);
            case LT: return evalBinaryOperator(BinaryOperator::lessThan, operator, this::evalToObj);
            case LE: return evalBinaryOperator(BinaryOperator::lessThanOrEqual, operator, this::evalToObj);
            case GT: return evalBinaryOperator(BinaryOperator::greaterThan, operator, this::evalToObj);
            case GE: return evalBinaryOperator(BinaryOperator::greaterThanOrEqual, operator, this::evalToObj);
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
            default: throw new IllegalExpressionException("Unary operator not supported for direct evaluation: "+operator.getValue());
        }
    }

    @Override
    public Object evalFunction(Node<NamedFunction> fn) {
        if (fn.getValue().isAggregating()) {
            return evalAggFunction(fn);
        }
        switch (fn.getValue()) {
            case firstNonNull: return functions.firstNonNull(evalChildrenToObj(fn));
            case greatest: return functions.greatest(evalChildrenToNumber(fn));
            case ifThenElse: return functions.ifThenElse(evalToBoolean(fn.child(0)), evalToObj(fn.child(1)), evalToObj(fn.child(2)));
            case isNotNull: return functions.isNotNull(evalToObj(fn.child(0)));
            case isNull: return functions.isNull(evalToObj(fn.child(0)));
            case least: return functions.least(evalChildrenToNumber(fn));
            case log: return fn.size() == 1
                    ? functions.log(evalToNumber(fn.child(0)))
                    : functions.log(evalToNumber(fn.child(0))) / functions.log(evalToNumber(fn.child(1)));
            case log10: return functions.log10(evalToNumber(fn.child(0)));
            case removeZeros: return functions.removeZeros(evalToNumber(fn.child(0)));

            // "not implemented yet" => null
            default: return null;
        }
    }

    private Double evalAggFunction(Node<NamedFunction> fn) {
        List<DataItem> items = fn.aggregate(new ArrayList<>(), Node::toDataItem, List::add, node -> node.getType() == NodeType.DATA_ITEM);
        if (items.isEmpty()) throw new IllegalExpressionException("Aggregate function used without data item");
        double[] val0 = (double[]) dataItemValues.get(items.get(0));
        double[] values = new double[val0.length];
        for (dataItemIndex = 0; dataItemIndex < values.length; dataItemIndex++) {
            Number value = evalToNumber(fn.child(0));
            values[dataItemIndex] = value == null ? Double.NaN : value.doubleValue();
        }
        dataItemIndex = 0;
        switch (fn.getValue()) {
            case avg: return functions.avg(values);
            case count: return functions.count(values);
            case max: return functions.max(values);
            case median: return functions.median(values);
            case min: return functions.min(values);
            case percentileCont: return functions.percentileCont(values, evalToNumber(fn.child(1)));
            case stddev: return functions.stddev(values);
            case stddevPop: return functions.stddevPop(values);
            case stddevSamp: return functions.stddevSamp(values);
            case sum: return functions.sum(values);
            case variance: return functions.variance(values);
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
        return functions.namedValue(value.getValue());
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
        try {
            return cast.apply(node.eval(this));
        } catch (RuntimeException ex) {
            throw new IllegalExpressionException(ex.getMessage()+"\n\t at: "+NormaliseConsumer.toExpression(node));
        }
    }

    private String evalToString(Node<?> node) {
        return eval(node, Typed::toStringTypeCoercion);
    }

    private Boolean evalToBoolean(Node<?> node) {
        return eval(node, Typed::toBooleanTypeCoercion);
    }

    private Number evalToNumber(Node<?> node) {
        return eval(node, Typed::toNumberTypeCoercion);
    }

    private Object evalToObj(Node<?> node) {
        return node.eval(this);
    }

    private List<?> evalChildrenToObj(Node<?> node) {
        return node.children().map(this::evalToObj).collect(toList());
    }

    private List<? extends Number> evalChildrenToNumber(Node<?> node) {
        return node.children().map(this::evalToNumber).collect(toList());
    }
}
