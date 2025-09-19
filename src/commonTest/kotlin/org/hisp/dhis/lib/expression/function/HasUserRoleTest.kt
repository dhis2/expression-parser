package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.ExpressionMode
import org.hisp.dhis.lib.expression.spi.ExpressionData
import org.hisp.dhis.lib.expression.spi.IllegalExpressionException
import kotlin.test.*

/**
 * Test of the `d2:hasUserRole` function.
 *
 * @author Jan Bernitt
 */
internal class HasUserRoleTest {

    @Test
    fun testHasUserRole_Null() {
        assertFalse(evaluate("d2:hasUserRole(null)", mapOf("USER_ROLES" to listOf("admin"))))
    }

    @Test
    fun testHasUserRole_NoData() {
        val ex = assertFailsWith(IllegalExpressionException::class) { evaluate("d2:hasUserRole(\"admin\")", mapOf()) }
        assertEquals("Supplementary data for user needs to be provided", ex.message)
    }

    @Test
    fun testHasUserRole() {
        assertTrue(evaluate("d2:hasUserRole(\"admin\")", mapOf("USER_ROLES" to listOf("admin"))))
        assertFalse(evaluate("d2:hasUserRole(\"admin\")", mapOf("USER_ROLES" to listOf("guest"))))
        assertTrue(evaluate("d2:hasUserRole(\"admin\")", mapOf("USER_ROLES" to listOf("foo","admin"))))
    }

    private fun evaluate(expression: String, supplementaryValues: Map<String, List<String>>): Boolean {
        val data: ExpressionData = ExpressionData().copy(supplementaryValues = supplementaryValues)
        return Expression(expression, ExpressionMode.RULE_ENGINE_ACTION).evaluate( { _: String -> null }, data) as Boolean
    }
}