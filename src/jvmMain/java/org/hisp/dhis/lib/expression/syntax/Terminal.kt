package org.hisp.dhis.lib.expression.syntax

import org.hisp.dhis.lib.expression.ast.Node
import org.hisp.dhis.lib.expression.ast.NodeType
import org.hisp.dhis.lib.expression.syntax.Literals.parse

/**
 * A [Terminal] is a building block of a grammar that does not consist and any smaller parts. They represent a
 * simple value or leaf in the AST.
 *
 *
 * In the java model [Terminal]s extends [Fragment] so that they can be mixed with them as argument building
 * blocks that make up a [Fragment].
 */
fun interface Terminal : Fragment {

    override fun parse(expr: Expr, ctx: ParseContext) {
        val type = literalOf()
        ctx.addNode(type, factory(), expr) { e: Expr? -> parse(e!!, type) }
    }

    override fun name(): String? {
        return literalOf().name
    }

    fun literalOf(): NodeType

    /**
     * @return The factory to use when creating the [Node] in the AST. `null` if no specific node type
     * should be used to represent the [Terminal]. In that case the node used depends on the [NodeType].
     */
    fun factory(): Node.Factory? {
        return null
    }

    /**
     * Attach a custom [Node] creation [Node.Factory].
     *
     * @param create the factory to use for this [Terminal].
     * @return this [Terminal] but using the provided [Node.Factory]
     */
    fun by(create: Node.Factory): Terminal {
        val self = this
        return object : Terminal {
            override fun literalOf(): NodeType {
                return self.literalOf()
            }

            override fun factory(): Node.Factory {
                return create
            }
        }
    }
}
