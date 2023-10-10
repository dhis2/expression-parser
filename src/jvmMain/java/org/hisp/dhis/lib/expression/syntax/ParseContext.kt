package org.hisp.dhis.lib.expression.syntax

import org.hisp.dhis.lib.expression.ast.Node
import org.hisp.dhis.lib.expression.ast.NodeType
import org.hisp.dhis.lib.expression.ast.Position

/**
 * When parsing the expression grammar the context has two roles:
 *
 *
 * 1. allow to emit nodes in the AST by using begin/end or add node methods 2. lookup named building blocks by name to
 * continue parsing
 *
 * @author Jan Bernitt
 */
interface ParseContext : FragmentContext {
    /*
        Building the AST
     */
    fun beginNode(type: NodeType, start: Position?, value: String, create: Node.Factory?)
    fun endNode(type: NodeType, end: Position?)

    /*
        Building the AST convenience methods
     */
    fun beginNode(type: NodeType, start: Position?, value: String) {
        beginNode(type, start, value, null)
    }

    fun addNode(type: NodeType, start: Position?, value: String, create: Node.Factory? = null) {
        beginNode(type, start, value, create)
        endNode(type, start?.offsetBy(value.length))
    }

    fun addNode(type: NodeType, expr: Expr, parse: (Expr) -> String) {
        addNode(type, null, expr, parse)
    }

    fun addNode(type: NodeType, factory: Node.Factory?, expr: Expr, parse: (Expr) -> String ) {
        val start = expr.marker()
        val s = expr.position()
        try {
            addNode(type, start, parse(expr), factory)
        } catch (ex: RuntimeException) {
            expr.error(s, ex.message)
        }
    }
}
