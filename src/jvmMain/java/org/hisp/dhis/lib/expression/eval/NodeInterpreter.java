package org.hisp.dhis.lib.expression.eval;

import org.hisp.dhis.lib.expression.ast.*;
import org.hisp.dhis.lib.expression.spi.DataItemType;

import java.time.LocalDate;
import java.util.function.Function;

/**
 * A {@link NodeInterpreter} interprets a root {@link Node} to calculate or derive some value from it.
 *
 * @param <T> type of the result of the interpretation
 * @author Jan Bernitt
 */
public interface NodeInterpreter<T> extends Function<Node<?>, T> {

    @Override
    @SuppressWarnings("unchecked")
    default T apply(Node<?> node) {
        return switch (node.getType()) {
            // complex nodes
            case UNARY_OPERATOR -> evalUnaryOperator((Node<UnaryOperator>) node);
            case BINARY_OPERATOR -> evalBinaryOperator((Node<BinaryOperator>) node);
            case ARGUMENT -> evalArgument((Node<Integer>) node);
            case PAR -> evalParentheses((Node<Void>) node);
            case FUNCTION -> evalFunction((Node<NamedFunction>) node);
            case MODIFIER -> evalModifier((Node<DataItemModifier>) node);
            case DATA_ITEM -> evalDataItem((Node<DataItemType>) node);
            case VARIABLE -> evalVariable((Node<VariableType>) node);

            // simple nodes
            case BOOLEAN -> evalBoolean((Node<Boolean>) node);
            case UID -> evalUid((Node<String>) node);
            case DATE -> evalDate((Node<LocalDate>) node);
            case NULL -> evalNull((Node<Void>) node);
            case NUMBER -> evalNumber((Node<Double>) node);
            case STRING -> evalString((Node<String>) node);
            case INTEGER -> evalInteger((Node<Integer>) node);
            case IDENTIFIER -> evalIdentifier(node);
            case NAMED_VALUE -> evalNamedValue((Node<NamedValue>) node);
        };
    }

    default T evalParentheses(Node<Void> group) {
        return group.child(0).eval(this);
    }

    default T evalArgument(Node<Integer> argument) {
        return argument.child(0).eval(this);
    }

    T evalBinaryOperator(Node<BinaryOperator> operator);

    T evalUnaryOperator(Node<UnaryOperator> operator);

    T evalFunction(Node<NamedFunction> function);

    T evalModifier(Node<DataItemModifier> modifier);

    T evalDataItem(Node<DataItemType> data);

    T evalVariable(Node<VariableType> variable);

    /*
    Simple nodes:
     */

    T evalNamedValue(Node<NamedValue> value);

    T evalNumber(Node<Double> value);

    T evalInteger(Node<Integer> value);

    T evalBoolean(Node<Boolean> value);

    T evalNull(Node<Void> value);

    T evalString(Node<String> value);

    T evalIdentifier(Node<?> value);

    T evalUid(Node<String> value);

    T evalDate(Node<LocalDate> value);
}
