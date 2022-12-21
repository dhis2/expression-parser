package org.hisp.dhis.expression.parse;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toUnmodifiableMap;

public final class FragmentContext implements NamedFragments {

    private final Map<String, NonTerminal> constants;
    private final Map<String, NonTerminal> functions;
    private final Map<String, NonTerminal> modifiers;

    public FragmentContext(List<NonTerminal> constants, List<NonTerminal> functions, List<NonTerminal> modifiers) {
        this.constants = mapByName(constants);
        this.functions = mapByName(functions);
        this.modifiers = mapByName(modifiers);
    }

    private static Map<String, NonTerminal> mapByName(List<NonTerminal> functions) {
        return functions.stream().collect(toUnmodifiableMap(NonTerminal::name, Function.identity()));
    }

    @Override
    public NonTerminal lookupFunction(String name) {
        return functions.get(name);
    }

    @Override
    public NonTerminal lookupModifier(String name) {
        return modifiers.get(name);
    }

    @Override
    public NonTerminal lookupConstant(String name) {
        return constants.get(name);
    }
}
