package org.hisp.dhis.lib.expression

import kotlin.test.Test
import kotlin.test.assertEquals

internal class StringExpressionTest {
    @Test
    fun testEmpty() {
        assertEquals("", evaluate("''"))
    }

    @Test
    fun testUnicode() {
        assertEquals(
            """
    hello
    world!😱 I think it works!
    """.trimIndent(), evaluate("'hello\\nworld!\\uD83D\\uDE31 I think it works!'"))
    }

    private fun evaluate(expression: String): String? {
        return Expression(expression).evaluate() as String?
    }
}
