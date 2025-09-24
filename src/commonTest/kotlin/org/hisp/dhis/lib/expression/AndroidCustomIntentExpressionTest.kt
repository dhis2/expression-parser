package org.hisp.dhis.lib.expression

import org.hisp.dhis.lib.expression.spi.ExpressionData
import org.hisp.dhis.lib.expression.spi.ParseException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests that the Android Custom Intent mode
 */
internal class AndroidCustomIntentExpressionTest {

    @Test
    fun testLiteralValues() {
        assertEquals("faJk7SeA5e", evaluate("'faJk7SeA5e'"))
        assertEquals(5.0, evaluate("5"))
        assertEquals(5.5, evaluate("5.5"))
        assertEquals(true, evaluate("true"))
    }

    @Test
    fun testSecondLevelOrgunit() {
        val data = ExpressionData().copy(
            programVariableValues = mapOf(
                "orgunit_path" to "/ImspTQPwCqd/O6uvpzGd5pu/YuQRtpLP10I/g8upMTyEZGZ"
            )
        )

        assertEquals("O6uvpzGd5pu", evaluate("d2:split(VAR{orgunit_path}, '/', 2)", data))
    }

    @Test
    fun testConcatenateUserOrgunit() {
        val data = ExpressionData().copy(
            programVariableValues = mapOf(
                "user_username" to "admin",
                "orgunit_code" to "OUC32"
            )
        )

        assertEquals(
            "admin_OUC32",
            evaluate("d2:concatenate(VAR{user_username}, '_', VAR{orgunit_code})", data)
        )
    }

    @Test
    fun testValidateExpression() {
        validate("VAR{orgunit_code}", valid = true)
        validate("VAR{invalid_var}", valid = false)
    }

    companion object {
        private fun evaluate(expression: String, data: ExpressionData = ExpressionData()): Any? {
            return Expression(expression, ExpressionMode.ANDROID_CUSTOM_INTENT_EXPRESSION).evaluate(data)
        }

        private fun validate(expression: String, valid: Boolean) {
            try {
                Expression(expression, ExpressionMode.ANDROID_CUSTOM_INTENT_EXPRESSION).validate(mapOf())
                assertTrue(valid)
            } catch (e: ParseException) {
                assertFalse(valid)
            }
        }
    }
}
