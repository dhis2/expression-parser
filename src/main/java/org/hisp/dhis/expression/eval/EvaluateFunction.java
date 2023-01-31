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
import org.hisp.dhis.expression.spi.ExpressionData;
import org.hisp.dhis.expression.spi.ExpressionFunctions;
import org.hisp.dhis.expression.spi.IllegalExpressionException;
import org.hisp.dhis.expression.spi.VariableValue;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

/**
 * A {@link NodeInterpreter} that calculates the expression result value
 * using a {@link ExpressionFunctions} to implement the named functions, modifiers and data loading.
 *
 * @author Jan Bernitt
 */
@RequiredArgsConstructor
class EvaluateFunction implements NodeInterpreter<Object> {

    private final ExpressionFunctions functions;
    private final ExpressionData data;
    private int dataItemIndex = 0;

    @Override
    public Object evalBinaryOperator(Node<BinaryOperator> operator) {

        switch (operator.getValue()) {
            case EQ: return evalBinaryOperator(BinaryOperator::equal, operator, this::evalToMixed);
            case NEQ: return evalBinaryOperator(BinaryOperator::notEqual, operator, this::evalToMixed);
            case AND: return evalBinaryOperator(BinaryOperator::and, operator, this::evalToBoolean);
            case OR: return evalBinaryOperator(BinaryOperator::or, operator, this::evalToBoolean);
            case LT: return evalBinaryOperator(BinaryOperator::lessThan, operator, this::evalToMixed);
            case LE: return evalBinaryOperator(BinaryOperator::lessThanOrEqual, operator, this::evalToMixed);
            case GT: return evalBinaryOperator(BinaryOperator::greaterThan, operator, this::evalToMixed);
            case GE: return evalBinaryOperator(BinaryOperator::greaterThanOrEqual, operator, this::evalToMixed);
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
        NamedFunction fnInfo = fn.getValue();
        if (fnInfo.isAggregating()) {
            return evalAggFunction(fn);
        }
        if (fnInfo == NamedFunction.subExpression) {
            return null; // return value of only data item in the expression from map
        }
        switch (fnInfo) {
            // common functions
            case firstNonNull: return functions.firstNonNull(evalToMixed(fn.children()));
            case greatest: return functions.greatest(evalToNumbers(fn.children()));
            case ifThenElse: return functions.ifThenElse(evalToBoolean(fn.child(0)), evalToMixed(fn.child(1)), evalToMixed(fn.child(2)));
            case isNotNull: return functions.isNotNull(evalToMixed(fn.child(0)));
            case isNull: return functions.isNull(evalToMixed(fn.child(0)));
            case least: return functions.least(evalToNumbers(fn.children()));
            case log: return fn.size() == 1
                    ? functions.log(evalToNumber(fn.child(0)))
                    : functions.log(evalToNumber(fn.child(0))) / functions.log(evalToNumber(fn.child(1)));
            case log10: return functions.log10(evalToNumber(fn.child(0)));
            case removeZeros: return functions.removeZeros(evalToNumber(fn.child(0)));

            // d2 functions
            case d2_addDays: return functions.d2_addDays(evalToDate(fn.child(0)), evalToNumber(fn.child(1)));
            case d2_ceil: return functions.d2_ceil(evalToNumber(fn.child(0)));
            case d2_concatenate: return functions.d2_concatenate(evalToStrings(fn.children()));
            case d2_count: return functions.d2_count(evalToVar(fn.child(0)));
            case d2_countIfValue: return functions.d2_countIfValue(evalToVar(fn.child(0)), evalToMixed(fn.child(1)));
            case d2_countIfZeroPos: return functions.d2_countIfZeroPos(evalToVar(fn.child(0)));
            case d2_daysBetween: return functions.d2_daysBetween(evalToDate(fn.child(0)), evalToDate(fn.child(1)));
            case d2_extractDataMatrixValue: return functions.d2_extractDataMatrixValue(evalToString(fn.child(0)), evalToString(fn.child(1)));
            case d2_floor: return functions.d2_floor(evalToNumber(fn.child(0)));
            case d2_hasUserRole: return functions.d2_hasUserRole(evalToString(fn.child(0)), data.getSupplementaryValues().get("USER"));
            case d2_hasValue: return functions.d2_hasValue(evalToVar(fn.child(0)));
            case d2_inOrgUnitGroup: return functions.d2_inOrgUnitGroup(evalToString(fn.child(0)), data.getProgramRuleVariableValues().get("org_unit"), data.getSupplementaryValues());
            case d2_lastEventDate: return functions.d2_lastEventDate(evalToVar(fn.child(0)));
            case d2_left: return functions.d2_left(evalToString(fn.child(0)), evalToInteger(fn.child(1)));
            case d2_length: return functions.d2_length(evalToString(fn.child(0)));
            case d2_maxValue: return functions.d2_maxValue(evalToVar(fn.child(0)));
            case d2_minutesBetween: return functions.d2_minutesBetween(evalToDate(fn.child(0)), evalToDate(fn.child(1)));
            case d2_minValue: return functions.d2_minValue(evalToVar(fn.child(0)));
            case d2_modulus: return functions.d2_modulus(evalToNumber(fn.child(0)), evalToNumber(fn.child(1)));
            case d2_monthsBetween: return functions.d2_monthsBetween(evalToDate(fn.child(0)), evalToDate(fn.child(1)));
            case d2_oizp: return functions.d2_oizp(evalToNumber(fn.child(0)));
            case d2_right: return functions.d2_right(evalToString(fn.child(0)), evalToInteger(fn.child(1)));
            case d2_round: return functions.d2_round(evalToNumber(fn.child(0)), fn.size() <= 1 ? Integer.valueOf(0) : evalToInteger(fn.child(1)));
            case d2_split: return functions.d2_split(evalToString(fn.child(0)), evalToString(fn.child(1)), evalToInteger(fn.child(2)));
            case d2_substring: return functions.d2_substring(evalToString(fn.child(0)), evalToInteger(fn.child(1)), evalToInteger(fn.child(2)));
            case d2_validatePattern: return functions.d2_validatePattern(evalToString(fn.child(0)), evalToString(fn.child(1)));
            case d2_weeksBetween: return functions.d2_weeksBetween(evalToDate(fn.child(0)), evalToDate(fn.child(1)));
            case d2_yearsBetween: return functions.d2_yearsBetween(evalToDate(fn.child(0)), evalToDate(fn.child(1)));
            case d2_zing: return functions.d2_zing(evalToNumber(fn.child(1)));
            case d2_zpvc: return functions.d2_zpvc(evalToNumbers(fn.children()));
            case d2_zScoreHFA: return functions.d2_zScoreHFA(evalToNumber(fn.child(0)), evalToNumber(fn.child(1)), evalToString(fn.child(2)));
            case d2_zScoreWFA: return functions.d2_zScoreWFA(evalToNumber(fn.child(0)), evalToNumber(fn.child(1)), evalToString(fn.child(2)));
            case d2_zScoreWFH: return functions.d2_zScoreWFH(evalToNumber(fn.child(0)), evalToNumber(fn.child(1)), evalToString(fn.child(2)));

            // "not implemented yet" => null
            default: return null;
        }
    }

    private Double evalAggFunction(Node<NamedFunction> fn) {
        List<DataItem> items = fn.aggregate(new ArrayList<>(), Node::toDataItem, List::add, node -> node.getType() == NodeType.DATA_ITEM);
        if (items.isEmpty()) throw new IllegalExpressionException("Aggregate function used without data item");
        double[] val0 = (double[]) data.getDataItemValues().get(items.get(0));
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
        Object value = data.getDataItemValues().get(item.toDataItem());
        return value != null && value.getClass().isArray()
                ? Array.get(value, dataItemIndex)
                : value;
    }

    @Override
    public Object evalVariable(Node<VariableType> variable) {
        String name = evalToString(variable.child(0));
        Map<String, ?> values = variable.getValue() == VariableType.PROGRAM
            ? data.getProgramVariableValues()
            : data.getProgramRuleVariableValues();
        if (!values.containsKey(name))
            throw new IllegalExpressionException(format("Unknown variable: '%s'", name));
        return values.get(name);
    }

    @Override
    public Object evalNamedValue(Node<NamedValue> value) {
        return data.getNamedValues().get(value.getRawValue());
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
            throw new IllegalExpressionException(ex.getMessage()+"\n\t at: "+ DescribeConsumer.toNormalisedExpression(node));
        }
    }

    private String evalToString(Node<?> node) {
        return eval(node, Typed::toStringTypeCoercion);
    }

    private Boolean evalToBoolean(Node<?> node) {
        return eval(node, Typed::toBooleanTypeCoercion);
    }

    private Double evalToNumber(Node<?> node) {
        return eval(node, Typed::toNumberTypeCoercion);
    }

    private Integer evalToInteger(Node<?> node) {
        Double val = evalToNumber(node);
        if (val == null) return null;
        if (val % 1d != 0d) throw new IllegalArgumentException("Expected an integer but got a floating point for: "+node);
        return val.intValue();
    }

    private LocalDate evalToDate(Node<?> node) {
        return eval(node, Typed::toDateTypeCoercion);
    }

    private Object evalToMixed(Node<?> node) {
        return node.eval(this);
    }

    private VariableValue evalToVar(Node<?> node) {
        return eval(node, VariableValue.class::cast);
    }

    private List<?> evalToMixed(Stream<Node<?>> nodes) {
        return evalToList(nodes, this::evalToMixed);
    }

    private List<Double> evalToNumbers(Stream<Node<?>> nodes) {
        return evalToList(nodes, this::evalToNumber);
    }

    private List<String> evalToStrings(Stream<Node<?>> nodes) {
        return evalToList(nodes, this::evalToString);
    }

    private static <T> List<T> evalToList(Stream<Node<?>> nodes, Function<Node<?>, T> map) {
        return nodes.map(map).collect(toList());
    }
}
