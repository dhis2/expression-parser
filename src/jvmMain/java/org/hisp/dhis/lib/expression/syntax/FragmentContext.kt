package org.hisp.dhis.lib.expression.syntax

import java.util.function.Function

fun interface FragmentContext {
    /*
        Lookup named building blocks (non-terminals)
    */
    fun fragment(name: String): Fragment?

    companion object {
        fun lookup(expr: Expr, parseName: Function<Expr, String>, lookup: Function<String, Fragment?>): Fragment {
            val s = expr.position()
            val name = parseName.apply(expr)
            val res = lookup.apply(name)
            if (res == null) {
                expr.error(s, "Unknown function or constant: '$name'")
            }
            return res!!
        }
    }
}
