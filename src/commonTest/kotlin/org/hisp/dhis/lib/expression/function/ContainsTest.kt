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
internal class ContainsTest {
    @Test
    fun testContains() {
        assertTrue(evaluate("contains('MOLD_ALLERGY,LATEX_ALLERGY', 'MOLD_ALLERGY')"))
        assertTrue(evaluate("contains('MOLD_ALLERGY,LATEX_ALLERGY', 'LATEX_ALLERGY', 'MOLD_ALLERGY')"))
        assertTrue(evaluate("contains('MOLD_ALLERGY,LATEX_ALLERGY', 'ALLERGY')"))
        assertTrue(evaluate("contains('MOLD_ALLERGY,LATEX_ALLERGY', 'RGY,LAT')"))
        assertTrue(evaluate("contains('abcdef', 'abcdef')"))
        assertTrue(evaluate("contains('abcdef', 'bcd')"))
        assertFalse(evaluate("contains('abcdef', 'xyz')"))
        assertTrue(evaluate("contains('abcdef')"))
    }

    @Test
    fun testContainsNoArgs() {
        assertFailsWith(IndexOutOfBoundsException::class) { evaluate("contains()") }
    }

    @Test
    fun testContainsOneArg() {
        assertTrue(evaluate("contains('abcdef')"))
    }

    private fun evaluate(expression: String): Boolean {
        return Expression(expression, Mode.INDICATOR_EXPRESSION).evaluate() as Boolean
    }
}
