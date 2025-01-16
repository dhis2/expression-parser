package org.hisp.dhis.lib.expression.operator

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.ExpressionMode.RULE_ENGINE_ACTION
import org.hisp.dhis.lib.expression.spi.ExpressionData
import org.hisp.dhis.lib.expression.spi.IllegalExpressionException
import org.hisp.dhis.lib.expression.spi.ValueType
import org.hisp.dhis.lib.expression.spi.VariableValue
import kotlin.test.*

/**
 * Port of the ANTLR `LogicalExpressionTest` and some more.
 */
internal class LogicalExpressionTest {
    @Test
    fun testNumericLiteralsAsBooleans() {
        assertEquals(true, evaluate("1 and 2"))
        assertEquals(true, evaluate("1 && 2"))
        assertEquals(false, evaluate("0 && 1"))
        assertEquals(false, evaluate("0 && 1"))
        assertEquals(false, evaluate("0 && 0"))
        assertEquals(false, evaluate("1.0 && 0"))
    }

    @Test
    fun testDoubleAsBoolean() {
        val ex = assertFailsWith(IllegalArgumentException::class) { evaluate("1.1 and 2") }
        assertEquals(
            "Failed to coerce value '1.1' (Double) to Boolean in expression: 1.1", ex.message)
    }

    @Test
    fun testAnd() {
        assertEquals(true, evaluate("true and true"))
        assertEquals(true, evaluate("true && true"))
        assertEquals(false, evaluate("true && false"))
        assertEquals(false, evaluate("false && true"))
        assertEquals(false, evaluate("false && false"))
    }

    @Test
    fun testOr() {
        assertEquals(true, evaluate("true or true"))
        assertEquals(true, evaluate("true || true"))
        assertEquals(true, evaluate("true || false"))
        assertEquals(true, evaluate("false || true"))
        assertEquals(false, evaluate("false || false"))
    }

    @Test
    fun testNot() {
        assertEquals(true, evaluate("!false"))
        assertEquals(false, evaluate("!true"))
        assertEquals(true, evaluate("not false"))
        assertEquals(false, evaluate("not true"))
    }

    @Test
    fun testEquality() {
        assertEquals(true, evaluate("true != false"))
        assertEquals(false, evaluate("true == false"))
    }

    @Test
    fun testShortCircuit() {
        val data = ExpressionData().copy(
            programRuleVariableValues = mapOf(
                Pair("event_date", VariableValue(ValueType.DATE)),
                Pair("vax1_prev", VariableValue(ValueType.DATE))
            )
        )
        val expression = "d2:daysBetween(d2:lastEventDate('vax1_prev'),V{event_date}) < 28"
        val unguarded =
            Expression(expression, RULE_ENGINE_ACTION)
        val ex = assertFailsWith(IllegalExpressionException::class) { unguarded.evaluate(data) }
        assertEquals("Failed to coerce value 'null' () to Any in expression: d2:daysBetween(d2:lastEventDate('vax1_prev'),V{event_date})", ex.message)

        val guardedAnd = Expression("d2:hasValue(#{vax1_prev}) && $expression", RULE_ENGINE_ACTION)
        assertFalse(guardedAnd.evaluate(data) as Boolean)

        val guardedOr = Expression("!d2:hasValue(#{vax1_prev}) || $expression", RULE_ENGINE_ACTION)
        assertTrue(guardedOr.evaluate(data) as Boolean)
    }

    companion object {
        private fun evaluate(expression: String): Boolean {
            return Expression(expression).evaluate() as Boolean
        }
    }
}
