package org.hisp.dhis.expression.eval;

import org.hisp.dhis.expression.ast.BinaryOperator;
import org.hisp.dhis.expression.ast.DataItemModifier;
import org.hisp.dhis.expression.ast.NamedFunction;
import org.hisp.dhis.expression.ast.NamedValue;
import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.ast.UnaryOperator;
import org.hisp.dhis.expression.spi.DataItemType;

import java.time.LocalDate;
import java.util.function.Function;

/**
 * A {@link NodeInterpreter} interprets a root {@link Node} to calculate or derive some value from it.
 *
 * @author Jan Bernitt
 *
 * @param <T> type of the result of the interpretation
 */
public interface NodeInterpreter<T> extends Function<Node<?>, T> {

    @Override
    @SuppressWarnings("unchecked")
    default T apply(Node<?> node) {
        switch(node.getType()) {
            // complex nodes
            case UNARY_OPERATOR: return evalUnaryOperator((Node<UnaryOperator>) node);
            case BINARY_OPERATOR: return evalBinaryOperator((Node<BinaryOperator>) node);
            case ARGUMENT: return evalArgument((Node<Integer>) node);
            case PAR: return evalParentheses((Node<Void>) node);
            case FUNCTION: return evalFunction((Node<NamedFunction>) node);
            case MODIFIER: return evalModifier((Node<DataItemModifier>) node);
            case DATA_ITEM: return evalDataItem((Node<DataItemType>) node);

            // simple nodes
            case BOOLEAN: return evalBoolean((Node<Boolean>) node);
            case UID: return evalUid((Node<String>) node);
            case DATE: return evalDate((Node<LocalDate>) node);
            case NULL: return evalNull((Node<Void>) node);
            case NUMBER: return evalNumber((Node<Double>) node);
            case STRING: return evalString((Node<String>) node);
            case INTEGER: return evalInteger((Node<Integer>) node);
            case IDENTIFIER: return evalIdentifier(node);
            case NAMED_VALUE: return evalNamedValue((Node<NamedValue>) node);

            default: throw new UnsupportedOperationException("Not type not supported yet: "+node.getType());
        }
    }

    default T evalParentheses(Node<Void> group) {
        return group.child(0).eval(this);
    }

    default T evalArgument(Node<Integer> argument) {
        return argument.child(0).eval(this);
    }

    T evalBinaryOperator(Node<BinaryOperator> operator) ;

    T evalUnaryOperator(Node<UnaryOperator> operator) ;

    T evalFunction(Node<NamedFunction> function) ;

    T evalModifier(Node<DataItemModifier> modifier) ;

    T evalDataItem(Node<DataItemType> data) ;

    /*
    Simple nodes:
     */

    T evalNamedValue(Node<NamedValue> value);

    T evalNumber(Node<Double> value) ;

    T evalInteger(Node<Integer> value);

    T evalBoolean(Node<Boolean> value);

    T evalNull(Node<Void> value);

    T evalString(Node<String> value);

    T evalIdentifier(Node<?> value);

    T evalUid(Node<String> value) ;

    T evalDate(Node<LocalDate> value);
}
