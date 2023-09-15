package org.hisp.dhis.lib.expression;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests some basic properties of {@link Expression#describe(Map)}
 */
class DescriptionExpressionTest {

    @Test
    void operatorsAreSpaced() {
        assertEquals("1 + 2", describe("1 + 2"));
        assertEquals("1 + 2", describe("1+2"));
        assertEquals("1 + 2", describe("1  +2"));
        assertEquals("1 + 2", describe("1+  2"));
    }

    @Test
    void operatorsRetainInputSyntax() {
        assertEquals("true and false", describe("true and false"));
        assertEquals("true && false", describe("true && false"));
        assertEquals("true or false", describe("true or false"));
        assertEquals("true || false", describe("true || false"));
        assertEquals(" not true", describe("not true"));
        assertEquals("!true", describe("! true"));
    }

    @Test
    void functionsWithVariableAreRetained() {
        assertEquals("d2:hasValue(#{var})", describe("d2:hasValue(#{var})"));
        assertEquals("d2:hasValue(#{AttributeA})", describe("d2:hasValue(#{AttributeA})"));
        assertEquals("d2:hasValue(foo)", describe("d2:hasValue(#{AttributeA})", Map.of("AttributeA", "foo")));
    }

    private static String describe(String expression) {
        return describe(expression, Map.of());
    }

    private static String describe(String expression, Map<String, String> displayNames) {
        return new Expression(expression, Expression.Mode.RULE_ENGINE_ACTION).describe(displayNames);
    }
}
