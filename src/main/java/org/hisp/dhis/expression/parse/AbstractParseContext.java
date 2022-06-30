package org.hisp.dhis.expression.parse;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractParseContext implements ParseContext
{
    private final Map<String, NonTerminal> constants;
    private final Map<String, NonTerminal> functions;
    private final Map<String, NonTerminal> methods;

    AbstractParseContext(List<NonTerminal> constants, List<NonTerminal> functions, List<NonTerminal> methods) {
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
