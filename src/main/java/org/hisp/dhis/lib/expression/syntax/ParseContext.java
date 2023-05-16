package org.hisp.dhis.lib.expression.syntax;

import org.hisp.dhis.lib.expression.ast.Node;
import org.hisp.dhis.lib.expression.ast.NodeType;
import org.hisp.dhis.lib.expression.ast.Position;

import java.util.function.Function;

/**
 * When parsing the expression grammar the context has two roles:
 *
 * 1. allow to emit nodes in the AST by using begin/end or add node methods
 * 2. lookup named building blocks by name to continue parsing
 *
 * @author Jan Bernitt
 */
public interface ParseContext extends FragmentContext
{
    /*
        Building the AST
     */

    void beginNode(NodeType type, Position start, String value, Node.Factory create );
    void endNode(NodeType type, Position end);

    /*
        Building the AST convenience methods
     */

    default void beginNode(NodeType type, Position start, String value ) {
        beginNode(type, start, value, null);
    }

    default void addNode(NodeType type, Position start, String value )
    {
        addNode(type, start, value, null);
    }

    default void addNode(NodeType type, Position start, String value, Node.Factory create )
    {
        beginNode( type,start, value, create );
        endNode(type, start == null ? null : start.offsetBy(value.length()));
    }
    default void addNode(NodeType type, Expr expr, Function<Expr, String> parse)
    {
        addNode(type, null, expr, parse);
    }
    default void addNode(NodeType type, Node.Factory factory, Expr expr, Function<Expr, String> parse)
    {
        Position start = expr.marker();
        int s = expr.position();
        try {
            addNode(type, start, parse.apply(expr), factory);
        } catch (RuntimeException ex)
        {
            expr.error(s, ex.getMessage());
        }
    }

}
