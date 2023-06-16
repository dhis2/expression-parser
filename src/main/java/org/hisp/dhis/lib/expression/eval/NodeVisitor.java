package org.hisp.dhis.lib.expression.eval;

import org.hisp.dhis.lib.expression.ast.*;
import org.hisp.dhis.lib.expression.spi.DataItemType;

import java.time.LocalDate;
import java.util.function.Consumer;

/**
 * Extended visitor for {@link Node}s by their {@link NodeType}.
 *
 * @author Jan Bernitt
 */
public interface NodeVisitor extends Consumer<Node<?>> {
    @Override
    @SuppressWarnings("unchecked")
    default void accept(Node<?> node) {
        switch (node.getType()) {
            // complex nodes
            case UNARY_OPERATOR -> visitUnaryOperator((Node<UnaryOperator>) node);
            case BINARY_OPERATOR -> visitBinaryOperator((Node<BinaryOperator>) node);
            case ARGUMENT -> visitArgument((Node<Integer>) node);
            case PAR -> visitParentheses((Node<Void>) node);
            case FUNCTION -> visitFunction((Node<NamedFunction>) node);
            case MODIFIER -> visitModifier((Node<DataItemModifier>) node);
            case DATA_ITEM -> visitDataItem((Node<DataItemType>) node);
            case VARIABLE -> visitVariable((Node<VariableType>) node);


            // simple nodes
            case BOOLEAN -> visitBoolean((Node<Boolean>) node);
            case UID -> visitUid((Node<String>) node);
            case DATE -> visitDate((Node<LocalDate>) node);
            case NULL -> visitNull((Node<Void>) node);
            case NUMBER -> visitNumber((Node<Double>) node);
            case STRING -> visitString((Node<String>) node);
            case INTEGER -> visitInteger((Node<Integer>) node);
            case IDENTIFIER -> visitIdentifier(node);
            case NAMED_VALUE -> visitNamedValue((Node<NamedValue>) node);
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

    default void visitDataItem(Node<DataItemType> data) {

    }

    default void visitVariable(Node<VariableType> variable) {

    }

    /*
    Simple nodes:
     */

    default void visitNamedValue(Node<NamedValue> value) {

    }

    default void visitNumber(Node<Double> value) {

    }

    default void visitInteger(Node<Integer> value) {

    }

    default void visitBoolean(Node<Boolean> value) {

    }

    default void visitNull(Node<Void> value) {

    }

    default void visitString(Node<String> value) {

    }

    default void visitIdentifier(Node<?> value) {
        // identifier nodes use both enums or String values so at this point we can't say what value we got
    }

    default void visitUid(Node<String> value) {

    }

    default void visitDate(Node<LocalDate> value) {

    }
}
