package org.hisp.dhis.lib.expression.util

import org.hisp.dhis.lib.expression.ast.Node
import org.hisp.dhis.lib.expression.ast.NodeType
import java.util.function.UnaryOperator

/**
 * A simplified tree structure useful to verify the [NodeType] structure of a [Node] AST.
 *
 * @author Jan Bernitt
 */
data class TNode(
    val type: NodeType,
    val children: MutableList<TNode>
) {

    fun add(type: NodeType): TNode {
        children.add(of(type))
        return this
    }

    fun add(type: NodeType, with: UnaryOperator<TNode>): TNode {
        children.add(with.apply(of(type)))
        return this
    }

    fun add(type: NodeType, vararg level1toN: NodeType): TNode {
        children.add(ofLevels(type, *level1toN))
        return this
    }

    companion object {
        fun ofLevels(root: NodeType, vararg level1toN: NodeType): TNode {
            val node = of(root)
            var target = node
            for (t in level1toN) {
                val c = of(t)
                target.children.add(c)
                target = c
            }
            return node
        }

        fun of(type: NodeType): TNode {
            return TNode(type, ArrayList())
        }

        fun of(root: Node<*>): TNode {
            return root.map(Node<*>::getType, ::TNode)
        }
    }
}
