package org.hisp.dhis.expression.ast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

/**
 * A node in the AST of the expression language.
 *
 * Nodes of the different {@link NodeType}s are implemented in {@link Nodes} by dedicated classes
 * but used through the {@link Node} interface.
 *
 * Generally there are two kinds of nodes:
 * <ul>
 *     <li>simple nodes with no children, for example numbers, strings, UIDs, ...</li>
 *     <li>complex nodes with can have children, for example operators, data items, functions, arguments</li>
 * </ul>
 *
 * A {@link Node} AST is a mutable data structure that is initially constructed during the parsing process.
 * Later it can be transformed using {@link #transform(java.util.function.UnaryOperator)}.
 */
public interface Node<T> extends Typed {

    /**
     * Creates a node of a specific type from a raw input value.
     *
     * This is used to customize the result node during the parsing process
     * by attaching a custom factory to a fragment.
     */
    interface Factory {

        Node<?> create(NodeType type, String rawValue);
    }

    /**
     * @return the type of this node, never null
     */
    NodeType getType();

    /**
     * @return The value of this node as present in the parsed input
     */
    String getRawValue();

    /**
     * @return The value of this node after being converted to the node's value type
     */
    T getValue();

    /**
     * Visits a subtree.
     * This node first, then its children in order recursively.
     *
     * @param visitor visitor to call for each matching node in the subtree
     * @param filter filter to restrict visitation to subset of nodes, filtered nodes are just not passed to the provided visitor
     */
    void visit(Consumer<Node<?>> visitor, Predicate<Node<?>> filter);


    /**
     * Visit a subtree but only nodes of the given {@link NodeType}.
     *
     * @param type the type of nodes to visit in the subtree including this node
     * @param visitor visitor to call for matching nodes
     */
    default void visit(NodeType type, Consumer<Node<?>> visitor) {
        visit(visitor, node -> node.getType() == type);
    }

    /**
     * Visits a subtree without exception.
     * This node first, then its children in order recursively.
     *
     * @param visitor visitor to call for each node in the subtree
     */
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

    /**
     * Interpretation of this node (subtree) using dynamic dispatch pattern.
     *
     * @param interpreter a interpreter function that converts this the subtree starting from this node to a value
     * @return result of the node interpretation
     * @param <R> type of the result value
     */
    default <R> R eval(Function<Node<?>, R> interpreter) {
        return interpreter.apply(this);
    }

    default void walkChildren(Consumer<Node<?>> walker, BiConsumer<Node<?>, Node<?>> separator) {
        Node<?> last = null;
        for (int i = 0; i < size(); i++)
        {
            Node<?> child = child(i);
            if (separator != null && last != null) {
                separator.accept(last, child);
            }
            child.walk(walker);
            last = child;
        }
    }

    /**
     * @return Number of child nodes of this node
     */
    default int size() {
        return 0;
    }

    /**
     * @return true, if this node has no children, else false
     */
    default boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Access the nth child node of this node.
     *
     * @param index the index to access
     * @return the child node at the given index
     * @throws IndexOutOfBoundsException when there is no child at the given index
     * @throws UnsupportedOperationException when this node cannot have children
     */
    default Node<?> child(int index) throws IndexOutOfBoundsException {
        throw new UnsupportedOperationException("Node of type "+getType()+" cannot have children.");
    }

    /**
     * Stream process this node's children.
     *
     * @return A stream of the child nodes. Empty of this node has no children.
     */
    default Stream<Node<?>> children() {
        return Stream.empty(); // by default: nothing to do assuming no children exist
    }

    /**
     * Adds a child to this node.
     * @param child the child to add
     * @throws UnsupportedOperationException when this node does not support children
     */
    default void addChild(Node<?> child) {
        throw new UnsupportedOperationException("Node of type "+getType()+" cannot have children.");
    }

    /**
     * Modifiers are not created directly from parsing but as a post parsing transformation of the AST.
     *
     * @param modifier add a modifier node to this node
     * @throws UnsupportedOperationException when this node does not support modifiers
     */
    default void addModifier(Node<?> modifier) {
        throw new UnsupportedOperationException("Node of type "+getType()+" cannot have modifiers.");
    }

    /**
     * Iterate this node's modifiers.
     *
     * @return all modifier nodes attached to this node, empty if there are none or a node cannot have modifiers
     */
    default Iterable<Node<?>> modifiers() {
        return List.of();
    }

    /**
     * AST transformation can change the children of nodes.
     *
     * After that children of this node have been updated the transformation is applied recursively to the remaining (new) children.
     *
     * The update function is only called for nodes which can have children.
     * It is called for such nodes even if the currently do not have any children.
     *
     * @param transformer the children update function applied to all nodes with children
     */
    default void transform(java.util.function.UnaryOperator<List<Node<?>>> transformer)
    {
        // by default: nothing to do assuming no children exist
    }

    /**
     * Restructuring the AST by moving the operands of unary and binary operators to become child nodes of the operator.
     *
     * The transformation is done in operator precedence to assure a semantically correct result tree.
     *
     * @see #groupUnaryOperators(Node, UnaryOperator)
     * @see #groupBinaryOperators(Node, BinaryOperator)
     *
     * @param root the node to start the transformation from
     */
    static void groupOperators(Node<?> root) {
        stream(UnaryOperator.values()).forEachOrdered(op -> groupUnaryOperators(root, op));
        stream(BinaryOperator.values()).forEachOrdered(op -> groupBinaryOperators(root, op));
    }

    /**
     * Restructuring the AST by moving the affected sibling next to the unary operator into the operator as its only child node.
     *
     * If this is done in operator precedence the correct evaluation tree structure is the result.
     *
     * @param root the node to start the transformation from
     * @param op the operator to transform
     */
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
     * Restructures the AST by moving the siblings before and after a binary operator into the binary operator.
     *
     * If this is done in operator precedence the correct evaluation tree structure is the result.
     *
     * @param root the node to start the transformation from
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

    /**
     * Modifiers affect data items only. However, they can be applied to data items directly or indirectly.
     * Within a function or round bracket that has a modifier all data items within the bracket body are affected.
     * <p>
     * This transformation moves modifiers from being {@link Node#children()} to be added as {@link Node#addModifier(Node)}.
     * <p>
     * This transformation should only be applied when the expression should be evaluated including resolving data items to their actual value.
     *
     * @param root the node to start the transformation from.
     */
    static void attachModifiers(Node<?> root) {
        root.transform(children -> {
            Predicate<Node<?>> isModifier = child -> child.getType() == NodeType.MODIFIER;
            if (children.stream().noneMatch(isModifier)) {
                return children;
            }
            // attach any modifier found on this level to any data item in the subtree of the child before them
            for (int i = 1; i < children.size(); i++) {
                Node<?> maybeModifier = children.get(i);
                if (maybeModifier.getType() == NodeType.MODIFIER) {
                    // go back 1 (or more if node before is a modifier)
                    int target = i-1;
                    while (target >= 0 && children.get(target).getType() == NodeType.MODIFIER) target--;
                    if (target >= 0) {
                        children.get(target).visit(NodeType.DATA_ITEM, modified ->
                                modified.addModifier(maybeModifier));
                    }
                }
            }
            return children.stream().filter(not(isModifier)).collect(toList());
        });
    }
}
