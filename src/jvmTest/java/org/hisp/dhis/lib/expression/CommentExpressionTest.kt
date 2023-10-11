package org.hisp.dhis.lib.expression

import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

/**
 * Tests focussed on use of comments in expressions.
 */
internal class CommentExpressionTest {
    @Test
    fun testCommentLiteral() {
        assertEquals(1.0, evaluate("/* a comment */1"))
    }

    @Test
    fun testCommentSpaceLiteral() {
        assertEquals(1.0, evaluate("/* a comment */ 1"))
    }

    @Test
    fun testCommentCommentLiteral() {
        assertEquals(1.0, evaluate("/* a comment *//* another */ 1"))
    }

    @Test
    fun testCommentSpaceCommentLiteral() {
        assertEquals(1.0, evaluate("/* a comment */  /**/1"))
    }

    @Test
    fun testCommentLiteralComment() {
        assertEquals(1.0, evaluate("/*before*/1/*after*/"))
    }

    @Test
    fun testCommentSpaceLiteralSpaceComment() {
        assertEquals(1.0, evaluate("/*before*/ 1 /*after*/"))
    }

    @Test
    fun testCommentCommentLiteralCommentComment() {
        assertEquals(1.0, evaluate("/*before*//*before2*/ 1 /*after*//*after2*/"))
    }

    @Test
    fun testCommentSpaceCommentLiteralCommentSpaceComment() {
        assertEquals(1.0, evaluate("/*before/  /**/1/**/ /*after*/"))
    }

    companion object {
        private fun evaluate(expression: String): Any? {
            return Expression(expression).evaluate()
        }
    }
}
