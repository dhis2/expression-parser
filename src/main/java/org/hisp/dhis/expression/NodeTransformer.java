package org.hisp.dhis.expression;

import java.time.LocalDateTime;
import java.util.function.Function;

public interface NodeTransformer<T> extends Function<Node<?>, T> {

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
            case METHOD: return evalMethod((Node<NamedMethod>) node);
            case DATA_VALUE: return evalDataValue((Node<DataValue>) node);

            // simple nodes
            case BOOLEAN: return evalBoolean((Node<Boolean>) node);
            case WILDCARD: return evalWildcard((Node<Void>) node);
            case UID: return evalUid((Node<String>) node);
            case DATE: return evalDate((Node<LocalDateTime>) node);
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

    T evalMethod(Node<NamedMethod> method) ;

    T evalDataValue(Node<DataValue> data) ;

    /*
    Simple nodes:
     */

    T evalNamedValue(Node<NamedValue> value);

    T evalNumber(Node<Double> value) ;

    T evalInteger(Node<Integer> value);

    T evalBoolean(Node<Boolean> value);

    T evalNull(Node<Void> value);

    T evalWildcard(Node<Void> value);

    T evalString(Node<String> value);

    T evalIdentifier(Node<?> value);

    T evalUid(Node<String> value) ;

    T evalDate(Node<LocalDateTime> value);
}
