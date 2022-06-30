package org.hisp.dhis.expression;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.util.Arrays.stream;

/**
 * A node in the AST of the expression language.
 */
public interface Node<T> {

    interface Factory {

        Node<?> create(NodeType type, String rawValue);
    }

    NodeType getType();

    String getRawValue();

    T getValue();

    /**
     * Visits this node first, then its children in order recursively.
     *
     * @param visitor visitor to call
     * @param filter filter to restrict visitation to subset of nodes, filtered nodes are just not passed to the provided visitor
     */
    void visit(Consumer<Node<?>> visitor, Predicate<Node<?>> filter);

    default void visit(NodeType type, Consumer<Node<?>> visitor) {
        visit(visitor, node -> node.getType() == type);
    }

    default void visit(Consumer<Node<?>> visitor) {
        visit(visitor, node -> true);
    }

    /**
     * Walking the AST is navigated by the walker.
     * Nodes will not implicitly walk their children.
     *
     * @param walker controls the walking
     */
    default void walk(Consumer<Node<?>> walker) {
        walker.accept(this);
    }

    default int size() {
        return 0;
    }

    default boolean isEmpty() {
        return size() == 0;
    }

    default Node<?> child(int index) {
        throw new UnsupportedOperationException("Node of type "+getType()+" cannot have children.");
    }

    default Node<?> child(int n, NodeType ofType) {
        throw new UnsupportedOperationException("Node of type "+getType()+" cannot have children.");
    }

    default void addChild(Node<?> child) {
        throw new UnsupportedOperationException("Node of type "+getType()+" cannot have children.");
    }

    default void forEachChild(Consumer<Node<?>> run) {
        for (int i = 0; i < size(); i++) {
            run.accept(child(i));
        }
    }

    default void transform(java.util.function.UnaryOperator<List<Node<?>>> transformer)
    {
        // by default: nothing to do assuming no children exist
    }

    static void groupOperators(Node<?> root) {
        stream(UnaryOperator.values()).forEachOrdered(op -> groupUnaryOperators(root, op));
        stream(BinaryOperator.values()).forEachOrdered(op -> groupBinaryOperators(root, op));
    }

    static void groupUnaryOperators(Node<?> root, UnaryOperator op)
    {
        root.transform(children -> {
            Predicate<Node<?>> isUnary = child -> child.getValue() == op && child.isEmpty();
            if (children.stream().noneMatch(isUnary)) {
                return children;
            }
            LinkedList<Node<?>> grouped = new LinkedList<>();
            Node<?> operand = children.get(children.size()-1);
            for (int i = children.size()-2; i >= 0; i--)
            {
                Node<?> operator = children.get(i);
                if (isUnary.test(operator)) {
                    operator.addChild(operand);
                } else {
                    grouped.addFirst(operand);
                }
                operand = operator;
            }
            grouped.addFirst(operand);
            return grouped;
        });
    }

    /**
     * Restructures the tree by moving the siblings before and after a binary operator into the binary operator.
     *
     * If this is done in operator precedence the correct evaluation tree structure is the result.
     *
     * @param root the node to start the transformation from.
     * @param op the operator to transform
     */
    static void groupBinaryOperators(Node<?> root, BinaryOperator op) {
        root.transform(children -> {
            Predicate<Node<?>> isBinary = child -> child.getValue() == op && child.isEmpty();
            if (children.stream().noneMatch(isBinary))
            {
                return children;
            }
            List<Node<?>> grouped = new ArrayList<>(children.size());
            grouped.add(children.get(0));
            for (int i = 1; i < children.size(); i++) {
                Node<?> child = children.get(i);
                if (isBinary.test(child)) {
                    child.addChild(grouped.remove(grouped.size()-1));
                    child.addChild(children.get(i+1));
                    grouped.add(child);
                    i++; // also skip the right-hand side of the operator
                } else {
                    grouped.add(child);
                }
            }
            return grouped;
        });
    }
}
