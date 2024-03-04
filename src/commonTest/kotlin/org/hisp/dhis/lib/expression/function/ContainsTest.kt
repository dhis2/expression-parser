package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.Expression.Mode
import kotlin.test.*

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
        assertFalse(evaluate("contains('abcdef', null)"))
    }

    @Test
    fun testContains_NoArgs() {
        assertFailsWith(IndexOutOfBoundsException::class) { evaluate("contains()") }
    }

    @Test
    fun testContains_OneArg() {
        val ex = assertFailsWith(IllegalArgumentException::class) { evaluate("contains('abcdef')") }
        assertEquals("allOf parameter of contains contain at least one value", ex.message)
    }

    private fun evaluate(expression: String): Boolean {
        return Expression(expression, Mode.INDICATOR_EXPRESSION).evaluate() as Boolean
    }
}
