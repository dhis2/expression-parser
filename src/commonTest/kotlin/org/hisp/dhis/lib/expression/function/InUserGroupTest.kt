package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.ExpressionMode
import org.hisp.dhis.lib.expression.spi.ExpressionData
import org.hisp.dhis.lib.expression.spi.FixedKey
import org.hisp.dhis.lib.expression.spi.IllegalExpressionException
import org.hisp.dhis.lib.expression.spi.SupplementaryKey
import kotlin.test.*

/**
 * Test of the `d2:inUserGroup` function.
 *
 * @author Zubair Asghar
 */
internal class InUserGroupTest {

    @Test
    fun testInUserGroup_Null() {
        assertFalse(evaluate("d2:inUserGroup(null)", mapOf(SupplementaryKey.Fixed(FixedKey.USER_GROUPS) to listOf("uidusgroup0"))))
    }

    @Test
    fun testInUserGroup_NoData() {
        val ex = assertFailsWith(IllegalExpressionException::class) { evaluate("d2:inUserGroup(\"uidougroup1\")", mapOf()) }
        assertEquals("Supplementary data for user needs to be provided", ex.message)
    }

    @Test
    fun testInUserGroup() {
        assertTrue(evaluate("d2:inUserGroup(\"uidusgroup0\")", mapOf(SupplementaryKey.Fixed(FixedKey.USER_GROUPS) to listOf("uidusgroup0"))))
        assertFalse(evaluate("d2:inUserGroup(\"uidusgroup0\")", mapOf(SupplementaryKey.Fixed(FixedKey.USER_GROUPS) to listOf("uidusgroup1"))))
    }

    private fun evaluate(expression: String, supplementaryValues: Map<SupplementaryKey, List<String>>): Boolean {
        val data: ExpressionData = ExpressionData().copy(supplementaryValues = supplementaryValues)
        return Expression(expression, ExpressionMode.RULE_ENGINE_ACTION).evaluate( { _: String -> null }, data) as Boolean
    }
}