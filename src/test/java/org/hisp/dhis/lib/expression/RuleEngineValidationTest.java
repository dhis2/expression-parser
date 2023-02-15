package org.hisp.dhis.lib.expression;

import org.hisp.dhis.lib.expression.spi.IllegalExpressionException;
import org.hisp.dhis.lib.expression.spi.Issue;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the validation as used in rule engine.
 *
 * @author Jan Bernitt
 */
class RuleEngineValidationTest {

    @Test
    void testUnaryNot() {
        validate("!(true || false)");
    }

    @Test
    void testUnaryNot_Warning() {
        assertWarning("!d2:floor(42)", "!d2:floor(42)",
                "Incompatible operand type for unary operator `!`, expected a BOOLEAN but was: NUMBER");
    }

    @Test
    void testUnaryNot_Error() {
        assertError("!d2:addDays('2000-01-01', 5)", "!d2:addDays('2000-01-01',5)",
                "Incompatible operand type for unary operator `!`, expected a BOOLEAN but was: DATE");
    }

    @Test
    void testUnaryNot_ErrorLiteral() {
        assertError("!45.6", "45.6",
                "Literal expression `45.6` cannot be converted to type BOOLEAN expected by operator `!`");
    }

    @Test
    void testUnaryPlus() {
        validate("+1");
        validate("++1");
        validate("+'42'");
        validate("+'42.5'");
        validate("+('4'-'2')");
    }

    @Test
    void testUnaryPlus_Warning() {
        assertWarning("+d2:concatenate('42')", "+d2:concatenate('42')",
                "Incompatible operand type for unary operator `+`, expected a NUMBER but was: STRING");
    }

    @Test
    void testUnaryPlus_Error() {
        assertError("+d2:addDays('2000-01-01', 5)", "+d2:addDays('2000-01-01',5)",
                "Incompatible operand type for unary operator `+`, expected a NUMBER but was: DATE");
    }

    @Test
    void testUnaryMinus() {
        validate("-1");
        validate("--1");
        validate("-'1'");
        validate("-'1.6'");
        validate("-true");
    }

    @Test
    void testUnaryMinus_Warning() {
        assertWarning("-d2:concatenate('42')", "-d2:concatenate('42')",
                "Incompatible operand type for unary operator `-`, expected a NUMBER but was: STRING");
    }

    @Test
    void testUnaryMinus_Error() {
        assertError("-d2:addDays('2000-01-01', 5)", "-d2:addDays('2000-01-01',5)",
                "Incompatible operand type for unary operator `-`, expected a NUMBER but was: DATE");
    }

    @Test
    void testBinaryExponent() {
        validate("9^2");
        validate("9^-2");
    }

    @Test
    void testBinaryExponent_Warning() {
        assertWarning("4^d2:concatenate('2')", "4 ^ d2:concatenate('2')",
                "Incompatible type for right operand of binary operator `^`, expected a NUMBER but was: STRING");
    }

    @Test
    void testBinaryExponent_Error() {
        assertError("d2:addDays('2000-01-01', 5)^42", "d2:addDays('2000-01-01',5) ^ 42",
                "Incompatible type for left operand of binary operator `^`, expected a NUMBER but was: DATE");
    }

    @Test
    void testBinaryMultiply() {
        validate("4*2");
        validate("4*-2");
    }

    //TODO test rest of the binary operators

    @Test
    void testD2AddDays() {
        validate("d2:addDays('2000-01-01', 5)");
    }

    @Test
    void testD2AddDays_Warning() {
        assertWarning("d2:addDays('2000-01-01', d2:concatenate('5'))", "d2:concatenate('5')",
                "Incompatible type for 2. argument of d2:addDays, expected NUMBER but was: STRING");
    }

    @Test
    void testD2AddDays_Error() {
        assertError("d2:addDays('2000-01-01', 'not-a-number')", "'not-a-number'",
                "Literal expression `'not-a-number'` cannot be converted to type NUMBER expected by function `d2:addDays`");
        assertError("d2:addDays(42, 5)", "42",
                "Literal expression `42` cannot be converted to type DATE expected by function `d2:addDays`");
    }

    //TODO more functions

    @Test
    void testProgramVariable() {
        validate("V{due_date}", Set.of("due_date", "enrollment_count"));
    }

    @Test
    void testProgramVariable_Error() {
        assertError("V{due_date}", "V{due_date}","Unknown variable: `due_date`");
    }

    @Test
    void testProgramRuleVariable() {
        validate("#{abc}", Set.of("abc", "def"));
        validate("A{abc}", Set.of("abc", "def"));
        validate("d2:count('abc')", Set.of("abc", "def"));
    }

    @Test
    void testProgramRuleVariable_Error() {
        assertError("#{abc}", "#{abc}", "Unknown variable: `abc`");
        assertError("A{abc}", "A{abc}", "Unknown variable: `abc`");
        assertError("d2:count('abc')", "'abc'", "Unknown variable: `abc`");
    }

    @Test
    void testConstant() {
        validate("C{x1234567890}", Set.of("a1234567890", "x1234567890"));
    }

    @Test
    void testConstant_Error() {
        assertError("C{x1234567890}", "C{x1234567890}", "Unknown constant: `x1234567890`");
    }

    static void assertWarning(String expression, String expectedPosition, String expectedWarning) {
        IllegalExpressionException ex = assertThrows(IllegalExpressionException.class, () -> validate(expression));
        assertEquals(0, ex.getErrors().size());
        assertEquals(1, ex.getWarnings().size());
        Issue warning = ex.getWarnings().get(0);
        assertEquals(expectedPosition, warning.getPosition().get());
        assertEquals(expectedWarning, warning.getMessage());
    }

    static void assertError(String expression, String expectedPosition, String expectedWarning) {
        IllegalExpressionException ex = assertThrows(IllegalExpressionException.class, () -> validate(expression));
        assertEquals(1, ex.getErrors().size());
        assertEquals(0, ex.getWarnings().size());
        Issue error = ex.getErrors().get(0);
        assertEquals(expectedPosition, error.getPosition().get());
        assertEquals(expectedWarning, error.getMessage());
    }

    static void validate(String expression) {
        validate(expression, Set.of());
    }

    static void validate(String expression, Set<String> variableNames) {
        Expression expr = new Expression(expression, Expression.Mode.RULE_ENGINE_ACTION);
        expr.validate(variableNames);
    }
}
