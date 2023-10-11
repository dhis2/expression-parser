package org.hisp.dhis.lib.expression.syntax

import org.hisp.dhis.lib.expression.ast.NodeType

/**
 * A [Fragment] is a building block of a grammar that consists of further blocks that either are [Fragment]s
 * themselves or are [Terminal]s.
 */
fun interface Fragment {

    /**
     * Parse this non-terminal token for the expr at its current position and use the provided context to emit nodes or
     * lookup named non-terminals.
     *
     * @param expr input to process
     * @param ctx  context to build the tree and lookup named non-terminals
     */
    fun parse(expr: Expr, ctx: ParseContext)

    /**
     * @return true, if this block is an optional argument in an argument list
     * @see .maybe
     */
    fun isMaybe(): Boolean { return false }

    fun isVarargs(): Boolean { return false }

    /**
     * @return the name of this block as used when doing a lookup for a named block
     * @see .named
     */
    fun name(): String? {
        return null
    }

    /**
     * This PEG parser is free of back-tracking. This means a universal "might occur" construct does not exist. The
     * (...)? block here only works in argument lists which will simply check the [.isMaybe] flag. If an
     * expected argument is optional (maybe) an omitted position is not an error but valid and the end of the argument
     * list is found. This also means that optional arguments can only occur at the end of the parameter list, not in
     * the middle.
     *
     * @return This block as optional block (only in argument lists)
     */
    fun maybe(): Fragment {
        class Maybe(body: Fragment) : Delegate(body) {
            override fun isMaybe(): Boolean {
                return true
            }
        }
        return this as? Maybe ?: Maybe(this)
    }

    fun plus(): Fragment {
        class Plus(body: Fragment) : Delegate(body) {
            override fun isVarargs(): Boolean {
                return true
            }
        }
        return this as? Plus ?: Plus(this)
    }

    fun star(): Fragment {
        return plus().maybe()
    }

    /**
     * By default, blocks are not named. Naming a block only serves the purpose of indexing the block as a named block
     * for later lookup.
     *
     *
     * Note that a name is a wrapper on the original block
     *
     * @return this block but with a name label attached to it
     */
    fun named(name: String): Fragment {
        class Named(val name: String, body: Fragment) : Delegate(body) {
            override fun name(): String {
                return name
            }
        }
        return Named(name, if (this is Named) to else this)
    }

    fun quoted(): Fragment {
        return Fragment { expr, ctx ->
            val c = expr.peek()
            val isQuoted = c == '\'' || c == '"'
            if (isQuoted) expr.gobble()
            parse(expr, ctx)
            if (isQuoted) expr.expect(c)
        }
    }

    abstract class Delegate protected constructor(val to: Fragment) : Fragment {
        override fun parse(expr: Expr, ctx: ParseContext) {
            to.parse(expr, ctx)
        }

        override fun isMaybe(): Boolean {
            return to.isMaybe()
        }

        override fun isVarargs(): Boolean {
            return to.isVarargs()
        }

        override fun name(): String? {
            return to.name()
        }
    }

    companion object {
        fun constant(type: NodeType, literal: String): Fragment {
            val constant = Fragment { expr, ctx -> ctx.addNode(type, expr.marker(), literal) }
            return constant.named(literal)
        }
    }
}
