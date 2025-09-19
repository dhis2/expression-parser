package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.ExpressionMode
import org.hisp.dhis.lib.expression.spi.ExpressionData
import org.hisp.dhis.lib.expression.spi.ValueType
import org.hisp.dhis.lib.expression.spi.VariableValue
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test of the `d2:inOrgUnitGroup` function.
 *
 * @author Jan Bernitt
 */
internal class InOrgUnitGroupTest {

    @Test
    fun testInOrgUnitGroup_Null() {
        val supplementaryValues = mapOf("uidougroup0" to listOf("uiduser0001", "uiduser0001"))
        val programRuleVariableValues = mapOf("org_unit" to VariableValue(ValueType.STRING))
        assertFalse(evaluate("d2:inOrgUnitGroup(null)", supplementaryValues, programRuleVariableValues))
    }

    @Test
    fun testInOrgUnitGroup() {
        val supplementaryValues = mapOf("uidougroup0" to listOf("uiduser0001", "uiduser0002"))
        val programRuleVariableValues = mapOf("org_unit" to VariableValue(ValueType.STRING).copy(value = "uiduser0001"))
        assertTrue(evaluate("d2:inOrgUnitGroup(\"uidougroup0\")", supplementaryValues, programRuleVariableValues))

    }

    @Test
    fun testInOrgUnitGroup_GroupNotDefined() {
        val programRuleVariableValues = mapOf("org_unit" to VariableValue(ValueType.STRING).copy(value = "uiduser0001"))
        assertFalse(evaluate("d2:inOrgUnitGroup(\"uidougroup5\")", mapOf(), programRuleVariableValues))
    }

    @Test
    fun testInOrgUnitGroup_OrgUnitNotDefined() {
        val supplementaryValues = mapOf("uidougroup0" to listOf("uiduser0001", "uiduser0002"))
        assertFalse(evaluate("d2:inOrgUnitGroup(\"uidougroup0\")", supplementaryValues, mapOf()))
    }

    private fun evaluate(
        expression: String,
        supplementaryValues: Map<String, List<String>>,
        programRuleVariableValues: Map<String, VariableValue>
    ): Boolean {
        val data: ExpressionData = ExpressionData().copy(
            supplementaryValues = supplementaryValues,
            programRuleVariableValues = programRuleVariableValues)
        return Expression(expression, ExpressionMode.RULE_ENGINE_ACTION).evaluate( { _: String -> null }, data) as Boolean
    }
}