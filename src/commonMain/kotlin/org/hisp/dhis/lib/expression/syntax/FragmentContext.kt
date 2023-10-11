package org.hisp.dhis.lib.expression.syntax

fun interface FragmentContext {
    /*
        Lookup named building blocks (non-terminals)
    */
    fun fragment(name: String): Fragment?

    companion object {
        fun lookup(expr: Expr, parseName: (Expr) -> String, lookup: (String) -> Fragment?): Fragment {
            val s = expr.position()
            val name = parseName(expr)
            val res = lookup(name)
            if (res == null) {
                expr.error(s, "Unknown function or constant: '$name'")
            }
            return res!!
        }
    }
}
