package org.hisp.dhis.lib.expression

import org.hisp.dhis.lib.expression.Expression.Mode

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests some basic properties of [Expression.describe]
 */
internal class DescriptionExpressionTest {
    @Test
    fun operatorsAreSpaced() {
        assertEquals("1 + 2", describe("1 + 2"))
        assertEquals("1 + 2", describe("1+2"))
        assertEquals("1 + 2", describe("1  +2"))
        assertEquals("1 + 2", describe("1+  2"))
    }

    @Test
    fun operatorsRetainInputSyntax() {
        assertEquals("true and false", describe("true and false"))
        assertEquals("true && false", describe("true && false"))
        assertEquals("true or false", describe("true or false"))
        assertEquals("true || false", describe("true || false"))
        assertEquals(" not true", describe("not true"))
        assertEquals("!true", describe("! true"))
    }

    @Test
    fun functionsWithVariableAreRetained() {
        assertEquals("d2:hasValue(#{var})", describe("d2:hasValue(#{var})"))
        assertEquals("d2:hasValue(#{AttributeA})", describe("d2:hasValue(#{AttributeA})"))
        assertEquals(
            "d2:hasValue(foo)",
            describe("d2:hasValue(#{AttributeA})", mapOf("AttributeA" to "foo")))
    }

    companion object {
        private fun describe(expression: String, displayNames: Map<String, String> = mapOf()): String {
            return Expression(expression, Mode.RULE_ENGINE_ACTION).describe(displayNames)
        }
    }
}
