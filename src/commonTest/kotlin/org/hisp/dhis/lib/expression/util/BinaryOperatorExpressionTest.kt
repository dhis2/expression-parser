package org.hisp.dhis.lib.expression.util

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.spi.ValueType
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BinaryOperatorExpressionTest {

    @Test
    fun testOperatorPrecedence() {
        // or has less precedence than >
        // if that would be false the expression would be invalid or not compute to a boolean
        val expression = Expression(
            "d2:hasValue(#{test_var_two}) || d2:count(#{test_var_one}) > 0",
            Expression.Mode.RULE_ENGINE_CONDITION)
        expression.validate(
            mapOf(
                "test_var_one" to ValueType.NUMBER,
                "test_var_two" to ValueType.STRING))
        assertEquals("d2:hasValue(#{test_var_two}) || d2:count(#{test_var_one}) > 0", expression.normalise())
    }
}
