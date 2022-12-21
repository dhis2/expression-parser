package org.hisp.dhis.expression.eval;

import org.hisp.dhis.expression.ast.BinaryOperator;
import org.hisp.dhis.expression.ast.DataItemType;
import org.hisp.dhis.expression.ast.NamedFunction;
import org.hisp.dhis.expression.ast.DataItemModifier;
import org.hisp.dhis.expression.ast.NamedValue;
import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.ast.NodeType;
import org.hisp.dhis.expression.ast.UnaryOperator;

import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * Extended visitor for {@link Node}s by their {@link NodeType}.
 *
 * @author Jan Bernitt
 */
public interface NodeVisitor extends Consumer<Node<?>>
{
    @Override
    @SuppressWarnings("unchecked")
    default void accept(Node<?> node) {
        switch(node.getType()) {
            // complex nodes
            case UNARY_OPERATOR: visitUnaryOperator((Node<UnaryOperator>) node); break;
            case BINARY_OPERATOR: visitBinaryOperator((Node<BinaryOperator>) node); break;
            case ARGUMENT: visitArgument((Node<Integer>) node); break;
            case PAR: visitParentheses((Node<Void>) node); break;
            case FUNCTION: visitFunction((Node<NamedFunction>) node); break;
            case MODIFIER: visitModifier((Node<DataItemModifier>) node); break;
            case DATA_ITEM: visitDataValue((Node<DataItemType>) node); break;

            // simple nodes
            case BOOLEAN: visitBoolean((Node<Boolean>) node); break;
            case WILDCARD: visitWildcard((Node<Void>) node); break;
            case UID: visitUid((Node<String>) node); break;
            case DATE: visitDate((Node<LocalDateTime>) node); break;
            case NULL: visitNull((Node<Void>) node); break;
            case NUMBER: visitNumber((Node<Double>) node); break;
            case STRING: visitString((Node<String>) node); break;
            case INTEGER: visitInteger((Node<Integer>) node); break;
            case IDENTIFIER: visitIdentifier(node); break;
            case NAMED_VALUE: visitNamedValue((Node<NamedValue>) node); break;

            default: throw new UnsupportedOperationException("Not type not supported yet: "+node.getType());
        }
    }

    default void visitParentheses(Node<Void> group) {
    }

    default void visitArgument(Node<Integer> argument) {

    }

    default void visitBinaryOperator(Node<BinaryOperator> operator) {

    }

    default void visitUnaryOperator(Node<UnaryOperator> operator) {

    }

    default void visitFunction(Node<NamedFunction> function) {

    }

    default void visitModifier(Node<DataItemModifier> modifier) {

    }

    default void visitDataValue(Node<DataItemType> data) {

    }

    /*
    Simple nodes:
     */

    default void visitNamedValue(Node<NamedValue> value)
    {

    }

    default void visitNumber(Node<Double> value) {

    }

    default void visitInteger(Node<Integer> value) {

    }

    default void visitBoolean(Node<Boolean> value)
    {

    }

    default void visitNull(Node<Void> value)
    {

    }

    default void visitWildcard(Node<Void> value)
    {

    }

    default void visitString(Node<String> value)
    {

    }

    default void visitIdentifier(Node<?> value)
    {
        // identifier nodes use both enums or String values so at this point we can't say what value we got
    }

    default void visitUid(Node<String> value) {

    }

    default void visitDate(Node<LocalDateTime> value)
    {

    }
}
