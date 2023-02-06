package org.hisp.dhis.lib.expression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringExpressionTest {

    @Test
    void testEmpty() {
        assertEquals("", evaluate("''"));
    }

    @Test
    void testUnicode() {
        assertEquals("hello\n" +
                "world!\uD83D\uDE31 I think it works!", evaluate("'hello\\nworld!\\uD83D\\uDE31 I think it works!'"));
    }

    private static String evaluate(String expression) {
        return (String) new Expression(expression).evaluate();
    }
}
