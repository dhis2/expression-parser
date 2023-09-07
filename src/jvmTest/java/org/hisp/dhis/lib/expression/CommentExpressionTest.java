package org.hisp.dhis.lib.expression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests focussed on use of comments in expressions.
 */
class CommentExpressionTest {

    @Test
    void testCommentLiteral() {
        assertEquals(1.0d, evaluate("/* a comment */1"));
    }

    @Test
    void testCommentSpaceLiteral() {
        assertEquals(1.0d, evaluate("/* a comment */ 1"));
    }

    @Test
    void testCommentCommentLiteral() {
        assertEquals(1.0d, evaluate("/* a comment *//* another */ 1"));
    }

    @Test
    void testCommentSpaceCommentLiteral() {
        assertEquals(1.0d, evaluate("/* a comment */  /**/1"));
    }

    @Test
    void testCommentLiteralComment() {
        assertEquals(1.0d, evaluate("/*before*/1/*after*/"));
    }

    @Test
    void testCommentSpaceLiteralSpaceComment() {
        assertEquals(1.0d, evaluate("/*before*/ 1 /*after*/"));
    }

    @Test
    void testCommentCommentLiteralCommentComment() {
        assertEquals(1.0d, evaluate("/*before*//*before2*/ 1 /*after*//*after2*/"));
    }

    @Test
    void testCommentSpaceCommentLiteralCommentSpaceComment() {
        assertEquals(1.0d, evaluate("/*before/  /**/1/**/ /*after*/"));
    }

    private static Object evaluate(String expression) {
        return new Expression(expression).evaluate();
    }
}
