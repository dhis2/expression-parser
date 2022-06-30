package org.hisp.dhis.expression.parse;

import java.util.function.Function;

public interface NamedContext {

    NonTerminal lookupFunction(String name );

    NonTerminal lookupMethod(String name );

    NonTerminal lookupConstant(String name);

    static NonTerminal lookup(Expr expr, Function<Expr, String> parseName, Function<String, NonTerminal> lookup) {
        int s = expr.position();
        String name = parseName.apply(expr);
        NonTerminal res = lookup.apply(name);
        if (res == null)
        {
            expr.error(s, "name not available in context: "+name);
        }
        return res;
    }
}
