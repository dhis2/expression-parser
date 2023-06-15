package org.hisp.dhis.lib.expression;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the annotation based features of the expressions.
 *
 * @author Jan Bernitt
 */
class AnnotatedExpressionTest {

    @Test
    void testSimpleNodeWithBeforeAndAfter() {
        assertDescribedWithWhitespace(" 1 ");
        assertDescribedWithWhitespace("/* hello*/'string'/*world*/ ");
    }

    @Test
    void testParNodeWithBeforeAndAfter() {
        assertDescribedWithWhitespace(" (1) ");
        assertDescribedWithWhitespace("/* hello*/('string')/*world*/ ");
    }

    @Test
    void testSimpleNodesNested() {
        assertDescribedWithWhitespace("1   + 2 /* comment */ + ( 12 - 3)");
    }

    @Test
    void testSimpleNodesNestedDeep() {
        assertDescribedWithWhitespace("1+( 12 - (3* 5.7 ) ) ");
    }

    @Test
    void testFunction() {
        assertDescribedWithWhitespace("firstNonNull( null , 12 )");
    }

    @Test
    void testVariableWithModifiers() {
        assertDescribedWithWhitespace(" V{event_count} .stageOffset( 12 )");
        assertDescribedWithWhitespace("1 + text", "1 + V{event_count} .stageOffset( 12 )",
                Map.of("event_count", "text"));
    }


    private void assertDescribedWithWhitespace(String expression) {
        assertDescribedWithWhitespace(expression, expression, Map.of());
    }

    private void assertDescribedWithWhitespace(String expected, String expression, Map<String, String> displayNames) {
        assertEquals(expected, evaluate(expression).describe(displayNames));
    }

    private static Expression evaluate(String expression) {
        return new Expression(expression, Expression.Mode.PROGRAM_INDICATOR_EXPRESSION, true);
    }
}
