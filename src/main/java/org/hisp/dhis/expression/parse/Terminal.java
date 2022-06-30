package org.hisp.dhis.expression.parse;

import org.hisp.dhis.expression.Node;
import org.hisp.dhis.expression.NodeType;

/**
 * A {@link Terminal} is a building block of a grammar that does not consist and any smaller parts. They represent a simple value or leaf in the AST.
 *
 * In the java model {@link Terminal}s extends {@link NonTerminal} so that they can be mixed with them as argument building blocks that make up a {@link NonTerminal}.
 */
public interface Terminal extends NonTerminal
{
    @Override
    default void parse( Expr expr, ParseContext ctx )
    {
        NodeType type = literalOf();
        ctx.addNode(type, factory(), expr, e -> Literals.parse(e, type));
    }

    @Override
    default String name() {
        return literalOf().name();
    }

    NodeType literalOf();

    /**
     * @return The factory to use when creating the {@link Node} in the AST.
     *  {@code null} if no specific node type should be used to represent the {@link Terminal}.
     *   In that case the node used depends on the {@link NodeType}.
     */
    default Node.Factory factory() {
        return null;
    }

    /**
     * Attach a custom {@link Node} creation {@link Node.Factory}.
     *
     * @param create the factory to use for this {@link Terminal}.
     * @return this {@link Terminal} but using the provided {@link Node.Factory}
     */
    default Terminal as(Node.Factory create) {
        Terminal self = this;
        return new Terminal() {
            @Override
            public NodeType literalOf() {
                return self.literalOf();
            }

            @Override
            public Node.Factory factory() {
                return create;
            }
        };
    }
}
