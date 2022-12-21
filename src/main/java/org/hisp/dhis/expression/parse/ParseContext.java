package org.hisp.dhis.expression.parse;

import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.ast.NodeType;

import java.util.function.Function;

/**
 * When parsing the expression grammar the context has two roles:
 *
 * 1. allow to emit nodes in the AST by using begin/end or add node methods
 * 2. lookup named building blocks by name to continue parsing
 *
 * @author Jan Bernitt
 */
public interface ParseContext
{
    /*
        Building the AST
     */

    void beginNode(NodeType type, String value, Node.Factory create );
    void endNode(NodeType type);

    /*
        Building the AST convenience methods
     */

    default void beginNode(NodeType type, String value ) {
        beginNode(type, value, null);
    }

    default void addNode(NodeType type, String value )
    {
        addNode(type, value, null);
    }

    default void addNode(NodeType type, String value, Node.Factory create )
    {
        beginNode( type, value, create );
        endNode(type);
    }
    default void addNode(NodeType type, Expr expr, Function<Expr, String> parse)
    {
        addNode(type, null, expr, parse);
    }
    default void addNode(NodeType type, Node.Factory factory, Expr expr, Function<Expr, String> parse)
    {
        int s = expr.position();
        try {
            addNode(type, parse.apply(expr), factory);
        } catch (RuntimeException ex)
        {
            expr.error(s, ex.getMessage());
        }
    }

    /*
        Lookup named building blocks (non-terminals)
    */

    NamedFragments fragments();
}
