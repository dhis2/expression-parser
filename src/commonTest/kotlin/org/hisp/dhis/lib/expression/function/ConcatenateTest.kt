package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test of the `d2:concatenate` function.
 *
 * @author Jan Bernitt
 */
internal class ConcatenateTest {

    @Test
    fun testConcatenate() {
        assertEquals("hello world", evaluate("d2:concatenate(\"hello\", \" world\")"))
    }

    private fun evaluate(expression: String): Any? {
        return Expression(expression, Expression.Mode.RULE_ENGINE_ACTION).evaluate()
    }
}