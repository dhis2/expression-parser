package org.hisp.dhis.expression.eval;

import org.hisp.dhis.expression.ast.BinaryOperator;
import org.hisp.dhis.expression.ast.DataItemType;
import org.hisp.dhis.expression.ast.NamedFunction;
import org.hisp.dhis.expression.ast.DataItemModifier;
import org.hisp.dhis.expression.ast.NamedValue;
import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.ast.NodeType;
import org.hisp.dhis.expression.ast.UID;
import org.hisp.dhis.expression.ast.UnaryOperator;
import org.hisp.dhis.expression.eval.DataItemBackend.DataItemModifiers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * A {@link NodeInterpreter} that calculates the expression result value
 * using a {@link ExpressionBackend} to implement the named functions, modifiers and data loading.
 *
 * @author Jan Bernitt
 */
public class CalcNodeInterpreter implements NodeInterpreter<Object> {

    private final ExpressionBackend backend;

    private DataItemModifiers modifiers = new DataItemModifiers();

    public CalcNodeInterpreter() {
        this((item, mods) -> item.toString());
    }
    public CalcNodeInterpreter(ExpressionBackend backend) {
        this.backend = backend;
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
            case ADD: return BinaryOperator.add(evalToNumber(left), evalToNumber(right));
            case SUB: return BinaryOperator.subtract(evalToNumber(left), evalToNumber(right));
            case MUL: return BinaryOperator.multiply(evalToNumber(left), evalToNumber(right));
            case DIV: return BinaryOperator.divide(evalToNumber(left), evalToNumber(right));
            case MOD: return BinaryOperator.modulo(evalToNumber(left), evalToNumber(right));
            case EXP: return BinaryOperator.exp(evalToNumber(left), evalToNumber(right));
            default: throw new UnsupportedOperationException();
        }
    }

    @Override
    public Object evalUnaryOperator(Node<UnaryOperator> operator) {
        Node<?> operand = operator.child(0);
        switch (operator.getValue()) {
            case NOT: return !evalToBoolean(operand);
            case PLUS: return evalToNumber(operand);
            case MINUS: return UnaryOperator.negate(evalToNumber(operand));
            case DISTINCT: return operand.eval(this); //TODO? distinct? only in SQL?
            default: throw new UnsupportedOperationException();
        }
    }

    @Override
    public Object evalFunction(Node<NamedFunction> function) {
        switch (function.getValue()) {
            case firstNonNull: return backend.firstNonNull(function.children()
                    .map(node -> node.eval(this)).collect(toList()));
        }

        //TODO for aggregate functions
        // - find all data items in the subtree
        // - fetch each of their values array (period as extra dimension) and store in map
        // - evaluate the subtree for each index of the resulting value array
        //   instead of loading the data items the function passed here needs to pick the value
        //   from a map => this object needs a map from DataItemId to values and an "current index"
        //   and the evalDataItem needs to first check that map before it loads via backend
        // - clear the map (because the values would only be valid when used with the same modifiers)
        return null;
    }

    @Override
    public Void evalModifier(Node<DataItemModifier> modifier) {
        Node<?> arg = modifier.child(0);
        switch (modifier.getValue()) {
            case aggregationType: modifiers.aggregationType = evalToString(arg); break;
            case maxDate: modifiers.maxDate = evalToDate(arg); break;
            case minDate: modifiers.minDate = evalToDate(arg); break;
            case periodOffset: modifiers.periodOffset = evalToNumber(arg).intValue(); break;
            case stageOffset: modifiers.stageOffset = evalToNumber(arg).intValue(); break;
        }
        // modifiers do not have a return value
        // they only modify the backend context
        return null;
    }

    @Override
    public Object evalDataItem(Node<DataItemType> item) {
        modifiers = new DataItemModifiers(); // reset
        // update modifiers for this item
        item.modifiers().forEach(mod -> mod.eval(this));
        //TODO subtree to List<UID>


        List<UID> ids = new ArrayList<>();
        for (int i = 0; i < item.size(); i++) {
            Node<?> arg = item.child(i);
            Node<?> param = arg.child(0);
            if (param.getType() == NodeType.IDENTIFIER) {

            } else {

            }
        }
        return this.backend.dataValue(null, modifiers);
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
    public LocalDateTime evalDate(Node<LocalDateTime> value) {
        return value.getValue();
    }

    /*
    Result Type conversion
     */

    private <T> T eval(Node<?> node, Function<Object, T> cast) {
        return cast.apply(node.eval(this));
    }

    private String evalToString(Node<?> node) {
        return eval(node, CalcNodeInterpreter::castToString);
    }

    private Boolean evalToBoolean(Node<?> node) {
        return eval(node, CalcNodeInterpreter::castToBoolean);
    }

    private Number evalToNumber(Node<?> node) {
        return eval(node, CalcNodeInterpreter::castToNumber);
    }

    private LocalDateTime evalToDate(Node<?> node) {
        return eval(node, CalcNodeInterpreter::castToDate);
    }

    static String castToString(Object value) {
        return (String) value;
    }

    static Boolean castToBoolean(Object value) {
        return (Boolean) value;
    }

    static Number castToNumber(Object value) {
        return (Number) value;
    }

    static LocalDateTime castToDate(Object value) {
        return (LocalDateTime) value;
    }

}
