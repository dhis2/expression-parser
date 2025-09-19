package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.ExpressionMode
import org.hisp.dhis.lib.expression.spi.*
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
        val programRuleVariableValues =
            mapOf("org_unit" to VariableValue(ValueType.STRING))
        assertFalse(
            evaluate(
                "d2:inOrgUnitGroup(null)",
                mapOf(
                    SupplementaryKey.Dynamic(DynamicKey.ORG_UNIT_GROUP_SET, "uidougroup0") to
                            listOf("uiduser0001", "uiduser0001")
                ),
                programRuleVariableValues
            )
        )
    }

    @Test
    fun testInOrgUnitGroup() {
        val programRuleVariableValues =
            mapOf("org_unit" to VariableValue(ValueType.STRING).copy(value = "uiduser0001"))
        assertTrue(
            evaluate(
                "d2:inOrgUnitGroup(\"uidougroup0\")",
                mapOf(
                    SupplementaryKey.Dynamic(DynamicKey.ORG_UNIT_GROUP_SET, "uidougroup0") to
                            listOf("uiduser0001", "uiduser0002")
                ),
                programRuleVariableValues
            )
        )
    }

    @Test
    fun testInOrgUnitGroup_GroupNotDefined() {
        val programRuleVariableValues =
            mapOf("org_unit" to VariableValue(ValueType.STRING).copy(value = "uiduser0001"))
        assertFalse(
            evaluate(
                "d2:inOrgUnitGroup(\"uidougroup5\")",
                emptyMap(),
                programRuleVariableValues
            )
        )
    }

    @Test
    fun testInOrgUnitGroup_OrgUnitNotDefined() {
        assertFalse(
            evaluate(
                "d2:inOrgUnitGroup(\"uidougroup0\")",
                mapOf(
                    SupplementaryKey.Dynamic(DynamicKey.ORG_UNIT_GROUP_SET, "uidougroup0") to
                            listOf("uiduser0001", "uiduser0002")
                ),
                emptyMap()
            )
        )
    }

    private fun evaluate(
        expression: String,
        supplementaryValues: Map<SupplementaryKey, List<String>>,
        programRuleVariableValues: Map<String, VariableValue>
    ): Boolean {
        val data: ExpressionData = ExpressionData().copy(
            supplementaryValues = supplementaryValues,
            programRuleVariableValues = programRuleVariableValues
        )
        return Expression(expression, ExpressionMode.RULE_ENGINE_ACTION)
            .evaluate({ _: String -> null }, data) as Boolean
    }
}
