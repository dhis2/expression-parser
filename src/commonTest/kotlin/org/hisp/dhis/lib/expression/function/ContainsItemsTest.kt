package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.Expression.Mode
import kotlin.test.*

/**
 * Tests the `contains` function.
 *
 * @author Jim Grace
 */
internal class ContainsItemsTest {
    @Test
    fun testContainsItems() {
        assertTrue(evaluate("containsItems('MOLD_ALLERGY,LATEX_ALLERGY', 'MOLD_ALLERGY')"))
        assertTrue(evaluate("containsItems('MOLD_ALLERGY,LATEX_ALLERGY', 'LATEX_ALLERGY', 'MOLD_ALLERGY')"))
        assertTrue(evaluate("containsItems('abcdef', 'abcdef')"))

        assertFalse(evaluate("containsItems('MOLD_ALLERGY,LATEX_ALLERGY', 'ALLERGY')"))
        assertFalse(evaluate("containsItems('MOLD_ALLERGY,LATEX_ALLERGY', 'RGY,LAT')"))
        assertFalse(evaluate("containsItems('abcdef', 'bcd')"))
        assertFalse(evaluate("containsItems('abcdef', 'xyz')"))
        assertFalse(evaluate("containsItems('abcdef', null)"))
    }

    @Test
    fun testContainsItems_NoArgs() {
        assertFailsWith(IndexOutOfBoundsException::class) { evaluate("containsItems()") }
    }

    @Test
    fun testContainsItems_OneArg() {
        val ex = assertFailsWith(IllegalArgumentException::class) { evaluate("containsItems('abcdef')") }
        assertEquals("allOf parameter of contains contain at least one value", ex.message)
    }

    private fun evaluate(expression: String): Boolean {
        return Expression(expression, Mode.INDICATOR_EXPRESSION).evaluate() as Boolean
    }
}
