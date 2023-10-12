package org.hisp.dhis.lib.expression.ast

import org.hisp.dhis.lib.expression.ast.BinaryOperator
import org.hisp.dhis.lib.expression.spi.DataItem
import org.hisp.dhis.lib.expression.spi.ID
import org.hisp.dhis.lib.expression.spi.Variable

/**
 * A node in the AST of the expression language.
 *
 *
 * Nodes of the different [NodeType]s are implemented in [Nodes] by dedicated classes but used through the
 * [Node] interface.
 *
 *
 * Generally there are two kinds of nodes:
 *
 *  * simple nodes with no children, for example numbers, strings, UIDs, ...
 *  * complex nodes with can have children, for example operators, data items, functions, arguments
 *
 *
 *
 * A [Node] AST is a mutable data structure that is initially constructed during the parsing process.
 * Later it can be transformed using [.transform]}.
 */
interface Node<T> : Typed, NodeAnnotations {
    /**
     * Creates a node of a specific type from a raw input value.
     *
     *
     * This is used to customize the result node during the parsing process by attaching a custom factory to a
     * fragment.
     */
    fun interface Factory {
        fun create(type: NodeType, rawValue: String): Node<*>

        companion object {

            fun new(factory: (type: NodeType, rawValue: String) -> Node<*>): Factory {
                return Factory { t, rawValue -> factory(t, rawValue)  }
            }
        }
    }

    /**
     * @return the type of this node, never null
     */
    fun getType(): NodeType

    /**
     * @return The value of this node as present in the parsed input
     */
    fun getRawValue(): String

    /**
     * @return The value of this node after being converted to the node's value type
     */
    fun getValue(): T

    /**
     * Visits a subtree. This node first, then its children in order recursively.
     *
     * @param visitor visitor to call for each matching node in the subtree
     * @param filter  filter to restrict visitation to subset of nodes, filtered nodes are just not passed to the
     * provided visitor
     */
    /**
     * Visits a subtree without exception. This node first, then its children in order recursively.
     *
     * @param visitor visitor to call for each node in the subtree
     */
    fun visit(visitor: (Node<*>) -> Unit, filter: (Node<*>) -> Boolean = { _: Node<*> -> true })

    /**
     * Visit a subtree but only nodes of the given [NodeType].
     *
     * @param type    the type of nodes to visit in the subtree including this node
     * @param visitor visitor to call for matching nodes
     */
    fun visit(type: NodeType, visitor: (Node<*>) -> Unit) {
        visit(visitor) { node: Node<*> -> node.getType() === type }
    }

    /**
     * Walking the AST is navigated by the walker. Nodes will not implicitly walk their children.
     *
     * @param walker controls the walking
     */
    fun walk(walker: (Node<*>) -> Unit) {
        walker(this)
    }

    fun walkChildren(walker: (Node<*>) -> Unit, separator: ((Node<*>, Node<*>) -> Unit)?) {
        var last: Node<*>? = null
        for (i in 0 until size()) {
            val child = child(i)
            if (separator != null && last != null) {
                separator(last, child)
            }
            child.walk(walker)
            last = child
        }
    }

    /**
     * Interpretation of this node (subtree) using dynamic dispatch pattern.
     *
     * @param interpreter a interpreter function that converts this the subtree starting from this node to a value
     * @param <R>         type of the result value
     * @return result of the node interpretation
    </R> */
    fun <R> eval(interpreter: (Node<*>) -> R): R? {
        return interpreter(this)
    }

    /**
     * Maps this tree to a new tree-structure.
     *
     * @param nodeMap maps a node to the value of the target node replacement
     * @param newNode creates a new node from a value and the mapped list of children
     * @param <N>     type of the nodes in the target tree structure
     * @param <V>     type of the values in each node in the target tree structure
     * @return root of the mapped tree
    </V></N> */
    fun <N, V> map(nodeMap: (Node<*>) -> V, newNode: (V, MutableList<N>) -> N): N {
        return eval { node: Node<*> ->
            newNode(nodeMap(node), node.children().map { n: Node<*> -> n.map(nodeMap, newNode) }.toMutableList())
        }!!
    }

    /**
     * Unfiltered aggregation.
     *
     * @see .aggregate
     */
    fun <A, E> aggregate(target: A, eval: (Node<*>) -> E, agg: (A, E) -> Any): A {
        return aggregate(target, eval, agg) { _: Node<*>? -> true }
    }

    /**
     * Filtered aggregation.
     *
     * In place aggregation of values from the subtree of this node.
     *
     * @param target the object aggregating the evaluated values
     * @param eval   evaluates each node of the subtree to a value for aggregation
     * @param agg    the function to add a non-null value to the aggregation state
     * @param filter filter to restrict visitation to subset of nodes, filtered nodes are not evaluated nor aggregated
     * @param <A>    type of aggregation state (result)
     * @param <E>    type of the elements added to the aggregation state
     * @return the initial aggregation value after the aggregation, this is the same instance that probably changed
     * internally
    </E></A> */
    fun <A, E> aggregate(target: A, eval: (Node<*>) -> E, agg: (A, E) -> Any, filter: (Node<*>) -> Boolean): A {
        visit({ node: Node<*> ->
            val value: E? = eval(node)
            if (value != null) {
                agg(target, value)
            }
        }, filter)
        return target
    }

    /**
     * @return Number of child nodes of this node
     */
    fun size(): Int {
        return 0
    }

    /**
     * @return true, if this node has no children, else false
     */
    fun isEmpty(): Boolean {
        return size() == 0;
    }

    /**
     * Annotated nodes also hold whitespace information.
     *
     * @return true, if this node has been annotated with start and end [Position]
     */
    fun isAnnotated(): Boolean {
        return getStart() != null && getEnd() != null
    }

    /**
     * Access the nth child node of this node.
     *
     * @param index the index to access
     * @return the child node at the given index
     * @throws IndexOutOfBoundsException     when there is no child at the given index
     * @throws UnsupportedOperationException when this node cannot have children
     */
    fun child(index: Int): Node<*> {
        throw UnsupportedOperationException("Node of type " + getType() + " cannot have children.")
    }

    /**
     * Stream process this node's children.
     *
     * @return A stream of the child nodes. Empty of this node has no children.
     */
    fun children(): Sequence<Node<*>> {
        return emptySequence() // by default: nothing to do assuming no children exist
    }

    /**
     * Adds a child to this node.
     *
     * @param child the child to add
     * @throws UnsupportedOperationException when this node does not support children
     */
    fun addChild(child: Node<*>): Node<T> {
        throw UnsupportedOperationException("Node of type " + getType() + " cannot have children.")
    }

    /**
     * Modifiers are not created directly from parsing but as a post parsing transformation of the AST.
     *
     * @param modifier add a modifier node to this node
     * @throws UnsupportedOperationException when this node does not support modifiers
     */
    fun addModifier(modifier: Node<*>): Node<T> {
        throw UnsupportedOperationException("Node of type " + getType() + " cannot have modifiers.")
    }

    /**
     * @return The [DataItem] equivalent of this node in case it is a data item node or null otherwise.
     */
    fun toDataItem(): DataItem? {
        return null
    }

    fun toIDs(): Sequence<ID> {
        val item = toDataItem()
        return if (item == null) emptySequence()
        else listOf(listOf(item.uid0), item.uid1, item.uid2).flatten().asSequence()
    }

    fun toVariable(): Variable? {
        return null
    }

    /**
     * Iterate this node's modifiers.
     *
     * @return all modifier nodes attached to this node, empty if there are none or a node cannot have modifiers
     */
    fun modifiers(): Iterable<Node<*>> {
        return listOf()
    }

    /**
     * AST transformation can change the children of nodes.
     *
     *
     * After that children of this node have been updated the transformation is applied recursively to the remaining
     * (new) children.
     *
     *
     * The update function is only called for nodes which can have children. It is called for such nodes even if the
     * currently do not have any children.
     *
     * @param transformer the children update function applied to all nodes with children
     */
    fun transform(transformer: (Node<*>, List<Node<*>>) -> List<Node<*>>) {
        // by default: nothing to do assuming no children exist
    }

    companion object {
        /**
         * Restructuring the AST by moving the operands of unary and binary operators to become child nodes of the
         * operator.
         *
         *
         * The transformation is done in operator precedence to assure a semantically correct result tree.
         *
         * @param root the node to start the transformation from
         * @see .groupUnaryOperators
         * @see .groupBinaryOperators
         */
        fun groupOperators(root: Node<*>) {
            groupBinaryOperators(root, BinaryOperator.EXP)
            UnaryOperator.entries.forEach { op: UnaryOperator -> groupUnaryOperators(root, op) }
            BinaryOperator.entries
                .filter  { op: BinaryOperator -> op !== BinaryOperator.EXP }
                .forEach { op: BinaryOperator -> groupBinaryOperators(root, op) }
        }

        /**
         * Restructuring the AST by moving the affected sibling next to the unary operator into the operator as its only
         * child node.
         *
         *
         * If this is done in operator precedence the correct evaluation tree structure is the result.
         *
         * @param root the node to start the transformation from
         * @param op   the operator to transform
         */
        private fun groupUnaryOperators(root: Node<*>, op: UnaryOperator) {
            root.transform { _: Node<*>, children: List<Node<*>> ->
                val isUnary = { child: Node<*> -> child.getValue() === op && child.isEmpty() }
                if (children.none(isUnary)) {
                    return@transform children
                }
                val grouped = ArrayDeque<Node<*>>()
                var operand = children[children.size - 1]
                for (i in children.size - 2 downTo 0) {
                    val operator = children[i]
                    if (isUnary(operator)) {
                        operator.addChild(operand)
                    }
                    else {
                        grouped.addFirst(operand)
                    }
                    operand = operator
                }
                grouped.addFirst(operand)
                grouped
            }
        }

        /**
         * Restructures the AST by moving the siblings before and after a binary operator into the binary operator.
         *
         *
         * If this is done in operator precedence the correct evaluation tree structure is the result.
         *
         * @param root the node to start the transformation from
         * @param op   the operator to transform
         */
        private fun groupBinaryOperators(root: Node<*>, op: BinaryOperator) {
            root.transform { _: Node<*>?, children: List<Node<*>> ->
                val isBinary = { child: Node<*> -> child.getValue() === op && child.isEmpty() }
                if (children.none(isBinary)) {
                    return@transform children
                }
                val grouped: MutableList<Node<*>> = ArrayList(children.size)
                grouped.add(children[0])
                var i = 1
                while (i < children.size) {
                    val operator = children[i]
                    if (isBinary(operator)) {
                        operator.addChild(grouped.removeAt(grouped.size - 1)) // left
                        var right = children[++i]
                        while (op === BinaryOperator.EXP && right.getType() === NodeType.UNARY_OPERATOR) {
                            operator.addChild(right)
                            right = children[++i]
                        }
                        operator.addChild(right) // right value
                        grouped.add(operator)
                    }
                    else {
                        grouped.add(operator)
                    }
                    i++
                }
                grouped
            }
        }
    }
}
