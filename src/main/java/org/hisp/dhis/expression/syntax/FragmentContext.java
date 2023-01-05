package org.hisp.dhis.expression.syntax;

import java.util.function.Function;

public interface FragmentContext {

        /*
        Lookup named building blocks (non-terminals)
    */

    NonTerminal fragment(String name);

    static NonTerminal lookup(Expr expr, Function<Expr, String> parseName, Function<String, NonTerminal> lookup) {
        int s = expr.position();
        String name = parseName.apply(expr);
        NonTerminal res = lookup.apply(name);
        if (res == null)
        {
            expr.error(s, "Unknown function or constant: '"+name+"'");
        }
        return res;
    }
}
