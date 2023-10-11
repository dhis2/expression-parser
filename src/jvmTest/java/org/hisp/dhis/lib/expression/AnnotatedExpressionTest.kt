package org.hisp.dhis.lib.expression

import kotlin.test.assertEquals
import kotlin.test.Test

/**
 * Tests the annotation based features of the expressions.
 *
 * @author Jan Bernitt
 */
internal class AnnotatedExpressionTest {
    @Test
    fun testSimpleNodeWithBeforeAndAfter() {
        assertDescribedWithWhitespace(" 1 ")
        assertDescribedWithWhitespace("/* hello*/'string'/*world*/ ")
        assertDescribedWithWhitespace(" false ")
        assertDescribedWithWhitespace(" true ")
        assertDescribedWithWhitespace(" null ")
        assertDescribedWithWhitespace(" 2.4 ")
        assertDescribedWithWhitespace(" [days] ")
        assertDescribedWithWhitespace(" #{deGroup:u1234567890} ")
    }

    @Test
    fun testParNodeWithBeforeAndAfter() {
        assertDescribedWithWhitespace(" (1) ")
        assertDescribedWithWhitespace("/* hello*/('string')/*world*/ ")
    }

    @Test
    fun testSimpleNodesNested() {
        assertDescribedWithWhitespace("1   + 2 /* comment */ + ( 12 - 3)")
    }

    @Test
    fun testSimpleNodesNestedDeep() {
        assertDescribedWithWhitespace("1+( 12 - (3* 5.7 ) ) ")
    }

    @Test
    fun testFunction() {
        assertDescribedWithWhitespace("firstNonNull( null , 12 )")
        assertDescribedWithWhitespace(" d2:daysBetween( '2000-01-01' , '2000-01-05' ) ^ 5 ")
    }

    @Test
    fun testUnaryOperator() {
        assertDescribedWithWhitespace(" ! true ")
        assertDescribedWithWhitespace(" not  true ")
        assertDescribedWithWhitespace(" distinct  true ")
    }

    @Test
    fun testVariableWithModifiers() {
        assertDescribedWithWhitespace(" V{event_count} .stageOffset( 12 )")
        assertDescribedWithWhitespace(
            "1 + text", "1 + V{event_count} .stageOffset( 12 )",
            mapOf("event_count" to "text"))
    }

    private fun assertDescribedWithWhitespace(expression: String) {
        assertDescribedWithWhitespace(expression, expression, mapOf())
    }

    private fun assertDescribedWithWhitespace(expected: String, expression: String, displayNames: Map<String, String>) {
        assertEquals(expected, evaluate(expression).describe(displayNames))
    }

    companion object {
        private fun evaluate(expression: String): Expression {
            return Expression(expression, Expression.Mode.PROGRAM_INDICATOR_EXPRESSION, true)
        }
    }
}
