package org.hisp.dhis.lib.expression.ast;

import org.hisp.dhis.lib.expression.spi.DataItem;
import org.hisp.dhis.lib.expression.spi.ID;
import org.hisp.dhis.lib.expression.spi.Variable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

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
 * Later it can be transformed using {@link #transform(BiFunction)}}.
 */
public interface Node<T> extends Typed, NodeAnnotations {

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
     * Interpretation of this node (subtree) using dynamic dispatch pattern.
     *
     * @param interpreter a interpreter function that converts this the subtree starting from this node to a value
     * @return result of the node interpretation
     * @param <R> type of the result value
     */
    default <R> R eval(Function<Node<?>, R> interpreter) {
        return interpreter.apply(this);
    }

    /**
     * Maps this tree to a new tree-structure.
     *
     * @param nodeMap maps a node to the value of the target node replacement
     * @param newNode creates a new node from a value and the mapped list of children
     * @return root of the mapped tree
     * @param <N> type of the nodes in the target tree structure
     * @param <V> type of the values in each node in the target tree structure
     */
    default <N,V> N map(Function<Node<?>, V> nodeMap, BiFunction<V, List<N>, N> newNode ) {
        return eval(node -> newNode.apply(nodeMap.apply(node), node.children().map(n -> n.map(nodeMap, newNode)).collect(toList())));
    }

    /**
     * Unfiltered aggregation.
     *
     * @see #aggregate(Object, Function, BiConsumer, Predicate)
     */
    default <A, E> A aggregate(A init, Function<Node<?>, E> eval, BiConsumer<A, E> agg) {
        return aggregate(init, eval, agg, node -> true);
    }

    /**
     * Filtered aggregation.
     *
     * In place aggregation of values from the subtree of this node.
     *
     * @param init the initial aggregation value
     * @param eval evaluates each node of the subtree to a value for aggregation
     * @param agg the function to add a non-null value to the aggregation state
     * @param filter filter to restrict visitation to subset of nodes, filtered nodes are not evaluated nor aggregated
     * @return the initial aggregation value after the aggregation, this is the same instance that probably changed internally
     * @param <A> type of aggregation state (result)
     * @param <E> type of the elements added to the aggregation state
     */
    default <A, E> A aggregate(A init, Function<Node<?>, E> eval, BiConsumer<A, E> agg, Predicate<Node<?>> filter) {
        visit(node -> {
            E value = eval.apply(node);
            if (value != null) {
                agg.accept(init, value);
            }
        }, filter);
        return init;
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
    default Node<T> addChild(Node<?> child) {
        throw new UnsupportedOperationException("Node of type "+getType()+" cannot have children.");
    }

    /**
     * Modifiers are not created directly from parsing but as a post parsing transformation of the AST.
     *
     * @param modifier add a modifier node to this node
     * @throws UnsupportedOperationException when this node does not support modifiers
     */
    default Node<T> addModifier(Node<?> modifier) {
        throw new UnsupportedOperationException("Node of type "+getType()+" cannot have modifiers.");
    }

    /**
     * @return The {@link DataItem} equivalent of this node in case it is a data item node or null otherwise.
     */
    default DataItem toDataItem() {
        return null;
    }

    default Stream<ID> toIDs() {
        DataItem item = toDataItem();
        return item == null
                ? Stream.empty()
                : concat(concat(Stream.of(item.getUid0()), item.getUid1().stream()), item.getUid2().stream());
    }

    default Variable toVariable() { return null; }

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
    default void transform(BiFunction<Node<?>,List<Node<?>>,List<Node<?>>> transformer)
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
        groupBinaryOperators(root, BinaryOperator.EXP);
        stream(UnaryOperator.values()).forEachOrdered(op -> groupUnaryOperators(root, op));
        stream(BinaryOperator.values()).filter(op -> op != BinaryOperator.EXP).forEachOrdered(op -> groupBinaryOperators(root, op));
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
        root.transform((node, children) -> {
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
        root.transform((node, children) -> {
            Predicate<Node<?>> isBinary = child -> child.getValue() == op && child.isEmpty();
            if (children.stream().noneMatch(isBinary))
            {
                return children;
            }
            List<Node<?>> grouped = new ArrayList<>(children.size());
            grouped.add(children.get(0));
            for (int i = 1; i < children.size(); i++) {
                Node<?> operator = children.get(i);
                if (isBinary.test(operator)) {
                    operator.addChild(grouped.remove(grouped.size()-1)); // left
                    Node<?> right = children.get(++i);
                    while (op == BinaryOperator.EXP && right.getType() == NodeType.UNARY_OPERATOR) {
                        operator.addChild(right);
                        right = children.get(++i);
                    }
                    operator.addChild(right); // right value
                    grouped.add(operator);
                } else {
                    grouped.add(operator);
                }
            }
            return grouped;
        });
    }

    /**
     * Adds whitespace to each node based on the {@link Position} information.
     *
     * @param root the node that is the effective root for the provided list of whitespace tokens
     * @param wsTokens the sequence of whitespace tokens for the entire expression
     */
    static void addWsTokens(Node<?> root, List<String> wsTokens) {
        addWsTokensInternal(root, wsTokens);
    }

    private static void addWsTokensInternal(Node<?> node, List<String> wsTokens) {
        Position start = node.getStart();
        Position end = node.getEnd();
        if (start == null || end == null) {
            if (node.size() > 0) node.children().forEachOrdered(child -> addWsTokensInternal(child, wsTokens));
            return;
        }
        int first = start.wsToken;
        int last = end.wsToken;
        int size = node.size();
        if (size == 0) {
            node.setWsTokens(wsTokens.subList(first, last));
            return; // no recursion
        }
        List<String> nodeWs = new ArrayList<>();
        // from start to first child:
        Node<?> c0 = node.child(0);
        nodeWs.addAll(wsTokens.subList(first, c0.getStart().wsToken));
        // any between children
        for (int i = 1; i < size; i++) {
            Node<?> cn = node.child(i-1);
            Node<?> cm = node.child(i);
            nodeWs.addAll(wsTokens.subList(cn.getEnd().wsToken, cm.getStart().wsToken));
        }
        // any after last child
        Node<?> ce = node.child(size-1);
        nodeWs.addAll(wsTokens.subList(ce.getEnd().wsToken, last));

        node.setWsTokens(nodeWs);

        // then recursively
        node.children().forEachOrdered(child -> addWsTokensInternal(child, wsTokens));
    }
}
