package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.ExpressionMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Test of the `d2:zScoreWFH` function.
 *
 * @author Jan Bernitt
 */
internal class ZScoreWFHTest {

    @Test
    fun testZScoreHFA_Null() {
        val ex = assertFailsWith(IllegalArgumentException::class) {evaluate("d2:zScoreWFH(30, 88.5, null)")}
        assertEquals("Gender cannot be null", ex.message)
        val ex2 = assertFailsWith(IllegalArgumentException::class) {evaluate("d2:zScoreWFH(30, null, \"m\")")}
        assertEquals("Weight cannot be null", ex2.message)
        val ex3 = assertFailsWith(IllegalArgumentException::class) {evaluate("d2:zScoreWFH(null, 88.5, \"m\")")}
        assertEquals("Parameter cannot be null", ex3.message)
    }

    @Test
    fun testZScoreHFA_Male() {
        assertEquals(-2.0, evaluate("d2:zScoreWFH(50, 2.8, \"m\")"))
        assertEquals(0.0, evaluate("d2:zScoreWFH(60, 6, \"male\")"))
    }

    @Test
    fun testZScoreHFA_Female() {
        assertEquals(-1.0, evaluate("d2:zScoreWFH(50, 3.1, \"f\")"))
        assertEquals(-3.0, evaluate("d2:zScoreWFH(60.0, 4.5, \"female\")"))
    }

    private fun evaluate(expression: String): Any? {
        return Expression(expression, ExpressionMode.RULE_ENGINE_ACTION).evaluate()
    }
}