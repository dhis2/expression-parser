package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.Expression.Mode

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
        assertFalse(evaluate("containsItems('MOLD_ALLERGY,LATEX_ALLERGY', 'ALLERGY')"))
        assertFalse(evaluate("containsItems('MOLD_ALLERGY,LATEX_ALLERGY', 'RGY,LAT')"))
        assertTrue(evaluate("containsItems('abcdef', 'abcdef')"))
        assertFalse(evaluate("containsItems('abcdef', 'bcd')"))
        assertFalse(evaluate("containsItems('abcdef', 'xyz')"))
    }

    @Test
    fun testContainsItemsNoArgs() {
        assertFailsWith(IndexOutOfBoundsException::class) { evaluate("containsItems()") }
    }

    @Test
    fun testContainsItemsOneArg() {
        assertTrue(evaluate("containsItems('abcdef')"))
    }

    private fun evaluate(expression: String): Boolean {
        return Expression(expression, Mode.INDICATOR_EXPRESSION).evaluate() as Boolean
    }
}
