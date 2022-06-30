package org.hisp.dhis.expression.parse;

public interface NamedContext {

    NonTerminal lookupFunction(String name );

    NonTerminal lookupMethod(String name );

    NonTerminal lookupConstant(String name);
}
