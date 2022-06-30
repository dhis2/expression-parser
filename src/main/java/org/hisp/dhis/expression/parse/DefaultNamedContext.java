package org.hisp.dhis.expression.parse;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class DefaultNamedContext implements NamedContext {

    private final Map<String, NonTerminal> constants;
    private final Map<String, NonTerminal> functions;
    private final Map<String, NonTerminal> methods;

    public DefaultNamedContext(List<NonTerminal> constants, List<NonTerminal> functions, List<NonTerminal> methods) {
        this.constants = mapByName(constants);
        this.functions = mapByName(functions);
        this.methods = mapByName(methods);
    }

    private static Map<String, NonTerminal> mapByName(List<NonTerminal> functions) {
        return functions.stream().collect(Collectors.toUnmodifiableMap(NonTerminal::name, Function.identity()));
    }

    @Override
    public final NonTerminal lookupFunction(String name) {
        return functions.get(name);
    }

    @Override
    public final NonTerminal lookupMethod(String name) {
        return methods.get(name);
    }

    @Override
    public final NonTerminal lookupConstant(String name) {
        return constants.get(name);
    }
}
