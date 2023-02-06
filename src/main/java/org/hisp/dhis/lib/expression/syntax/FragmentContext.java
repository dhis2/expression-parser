package org.hisp.dhis.lib.expression.syntax;

import java.util.function.Function;

public interface FragmentContext {

        /*
        Lookup named building blocks (non-terminals)
    */

    Fragment fragment(String name);

    static Fragment lookup(Expr expr, Function<Expr, String> parseName, Function<String, Fragment> lookup) {
        int s = expr.position();
        String name = parseName.apply(expr);
        Fragment res = lookup.apply(name);
        if (res == null)
        {
            expr.error(s, "Unknown function or constant: '"+name+"'");
        }
        return res;
    }
}
