package org.hisp.dhis.lib.expression

import org.hisp.dhis.lib.expression.spi.IllegalExpressionException
import org.hisp.dhis.lib.expression.spi.ValueType
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test

/**
 * Tests the validation as used in rule engine.
 *
 * @author Jan Bernitt
 */
internal class RuleEngineValidationTest {
    @Test
    fun testUnaryNot() {
        validate("!(true || false)")
    }

    @Test
    fun testUnaryNot_Warning() {
        assertWarning(
            "!d2:floor(42)", "!d2:floor(42)",
            "Incompatible operand type for unary operator `!`, expected a BOOLEAN but was: NUMBER")
    }

    @Test
    fun testUnaryNot_Error() {
        assertError(
            "!d2:addDays('2000-01-01', 5)", "!d2:addDays('2000-01-01',5)",
            "Incompatible operand type for unary operator `!`, expected a BOOLEAN but was: DATE")
    }

    @Test
    fun testUnaryNot_ErrorLiteral() {
        assertError(
            "!45.6", "45.6",
            "Literal expression `45.6` cannot be converted to type BOOLEAN expected by operator `!`")
    }

    @Test
    fun testUnaryPlus() {
        validate("+1")
        validate("++1")
        validate("+'42'")
        validate("+'42.5'")
        validate("+('4'-'2')")
    }

    @Test
    fun testUnaryPlus_Warning() {
        assertWarning(
            "+d2:concatenate('42')", "+d2:concatenate('42')",
            "Incompatible operand type for unary operator `+`, expected a NUMBER but was: STRING")
    }

    @Test
    fun testUnaryPlus_Error() {
        assertError(
            "+d2:addDays('2000-01-01', 5)", "+d2:addDays('2000-01-01',5)",
            "Incompatible operand type for unary operator `+`, expected a NUMBER but was: DATE")
    }

    @Test
    fun testUnaryMinus() {
        validate("-1")
        validate("--1")
        validate("-'1'")
        validate("-'1.6'")
        validate("-true")
    }

    @Test
    fun testUnaryMinus_Warning() {
        assertWarning(
            "-d2:concatenate('42')", "-d2:concatenate('42')",
            "Incompatible operand type for unary operator `-`, expected a NUMBER but was: STRING")
    }

    @Test
    fun testUnaryMinus_Error() {
        assertError(
            "-d2:addDays('2000-01-01', 5)", "-d2:addDays('2000-01-01',5)",
            "Incompatible operand type for unary operator `-`, expected a NUMBER but was: DATE")
    }

    @Test
    fun testBinaryExponent() {
        validate("9^2")
        validate("9^-2")
    }

    @Test
    fun testBinaryExponent_Warning() {
        assertWarning(
            "4^d2:concatenate('2')", "4 ^ d2:concatenate('2')",
            "Incompatible type for right operand of binary operator `^`, expected a NUMBER but was: STRING")
    }

    @Test
    fun testBinaryExponent_Error() {
        assertError(
            "d2:addDays('2000-01-01', 5)^42", "d2:addDays('2000-01-01',5) ^ 42",
            "Incompatible type for left operand of binary operator `^`, expected a NUMBER but was: DATE")
    }

    @Test
    fun testBinaryMultiply() {
        validate("4*2")
        validate("4*-2")
    }

    //TODO test rest of the binary operators
    @Test
    fun testD2AddDays() {
        validate("d2:addDays('2000-01-01', 5)")
    }

    @Test
    fun testD2AddDays_Warning() {
        assertWarning(
            "d2:addDays('2000-01-01', d2:concatenate('5'))", "d2:concatenate('5')",
            "Incompatible type for 2. argument of d2:addDays, expected NUMBER but was: STRING")
    }

    @Test
    fun testD2AddDays_Error() {
        assertError(
            "d2:addDays('2000-01-01', 'not-a-number')", "'not-a-number'",
            "Literal expression `'not-a-number'` cannot be converted to type NUMBER expected by function `d2:addDays`")
        assertError(
            "d2:addDays(42, 5)", "42",
            "Literal expression `42` cannot be converted to type DATE expected by function `d2:addDays`")
    }

    //TODO more functions
    @Test
    fun testProgramVariable() {
        validate(
            "V{due_date}", mapOf(
                "due_date" to ValueType.DATE,
                "enrollment_count" to ValueType.NUMBER))
    }

    @Test
    fun testProgramVariable_Error() {
        assertError("V{due_date}", "V{due_date}", "Unknown variable: `due_date`")
    }

    @Test
    fun testProgramRuleVariable() {
        val variables = mapOf("abc" to ValueType.STRING)
        validate("#{abc}", variables)
        validate("A{abc}", variables)
        validate("d2:count('abc')", variables)
    }

    @Test
    fun testProgramRuleVariable_Error() {
        assertError("#{abc}", "#{abc}", "Unknown variable: `abc`")
        assertError("A{abc}", "A{abc}", "Unknown variable: `abc`")
        assertError("d2:count('abc')", "'abc'", "Unknown variable: `abc`")
    }

    @Test
    fun testProgramRuleVariable_Error_ActualType() {
        assertError(
            "d2:floor(#{due_date})", "#{due_date}",
            "Incompatible type for 1. argument of d2:floor, expected NUMBER but was: DATE",
            mapOf("due_date" to ValueType.DATE))
    }

    @Test
    fun testConstant() {
        validate(
            "C{x1234567890}", mapOf(
                "a1234567890" to ValueType.NUMBER,
                "x1234567890" to ValueType.NUMBER))
    }

    @Test
    fun testConstant_Error() {
        assertError("C{x1234567890}", "C{x1234567890}", "Unknown constant: `x1234567890`")
    }

    companion object {

        fun assertWarning(expression: String, expectedPosition: String, expectedWarning: String) {
            val ex = assertFailsWith(IllegalExpressionException::class) { validate(expression) }
            assertEquals(0, ex.getErrors().size)
            assertEquals(1, ex.getWarnings().size)
            val (position, message) = ex.getWarnings()[0]
            assertEquals(expectedPosition, position.invoke())
            assertEquals(expectedWarning, message)
        }

        fun assertError(
            expression: String,
            expectedPosition: String,
            expectedWarning: String,
            variables: Map<String, ValueType> = mapOf()
        ) {
            val ex = assertFailsWith(IllegalExpressionException::class) { validate(expression, variables) }
            assertEquals(1, ex.getErrors().size)
            assertEquals(0, ex.getWarnings().size)
            val (position, message) = ex.getErrors()[0]
            assertEquals(expectedPosition, position.invoke())
            assertEquals(expectedWarning, message)
        }

        fun validate(expression: String, variables: Map<String, ValueType> = mapOf()) {
            val expr = Expression(expression, Expression.Mode.RULE_ENGINE_ACTION)
            expr.validate(variables)
        }
    }
}
