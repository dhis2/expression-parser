package org.hisp.dhis.lib.expression

import org.hisp.dhis.lib.expression.spi.DataItem
import org.hisp.dhis.lib.expression.spi.DataItemType
import org.hisp.dhis.lib.expression.spi.ID
import org.hisp.dhis.lib.expression.spi.QueryModifiers
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Tests all forms of [DataItem]s are correctly transformed from an expression to an AST and into a
 * [DataItem] value.
 *
 * @author Jan Bernitt
 */
internal class DataItemExpressionTest {
    @Test
    fun testDataElement() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.DATA_ELEMENT,
                    ID(ID.Type.DataElementUID, "u1234567890"))),
            evaluate("#{u1234567890}"))
    }

    @Test
    fun testDataElement2() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.DATA_ELEMENT,
                    ID(ID.Type.DataElementUID, "u1234567890")),
                DataItem(
                    DataItemType.DATA_ELEMENT,
                    ID(ID.Type.DataElementUID, "v1234567890"))),
            evaluate("#{u1234567890} + #{v1234567890}"))
    }

    @Test
    fun testDataElementGroup() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.DATA_ELEMENT,
                    ID(ID.Type.DateElementGroupUID, "u1234567890"))),
            evaluate("#{deGroup:u1234567890}"))
    }

    @Test
    fun testProgramStageDataElement() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.DATA_ELEMENT,
                    ID(ID.Type.ProgramStageUID, "u1234567890"),
                    ID(ID.Type.DataElementUID, "v1234567890"))),
            evaluate("#{u1234567890.v1234567890}"))
    }

    @Test
    fun testProgramStageCategoryOption() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.DATA_ELEMENT,
                    ID(ID.Type.ProgramStageUID, "u1234567890"),
                    ID(ID.Type.CategoryOptionUID, "v1234567890"))),
            evaluate("#{u1234567890.co:v1234567890}"))
    }

    @Test
    fun testProgramStageCategoryOptionGroup() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.DATA_ELEMENT,
                    ID(ID.Type.ProgramStageUID, "u1234567890"),
                    listOf(
                        ID(ID.Type.CategoryOptionGroupUID, "v1234567890"),
                        ID(ID.Type.CategoryOptionGroupUID, "w1234567890")), listOf(),
                    QueryModifiers())),
            evaluate("#{u1234567890.coGroup:v1234567890&w1234567890}"))
    }

    @Test
    fun testDataElementCategoryOptionAttributeOptionCombo() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.DATA_ELEMENT,
                    ID(ID.Type.DataElementUID, "u1234567890"),
                    ID(ID.Type.CategoryOptionUID, "v1234567890"),
                    ID(ID.Type.AttributeOptionComboUID, "w1234567890"))),
            evaluate("#{u1234567890.v1234567890.w1234567890}"))
    }

    @Test
    fun testAttribute() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.ATTRIBUTE,
                    ID(ID.Type.AttributeUID, "u1234567890"))),
            evaluate("A{u1234567890}"))
    }

    @Test
    fun testProgramAttribute() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.ATTRIBUTE,
                    ID(ID.Type.ProgramUID, "u1234567890"),
                    ID(ID.Type.AttributeUID, "v1234567890"))),
            evaluate("A{u1234567890.v1234567890}"))
    }

    @Test
    fun testConstant() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.CONSTANT,
                    ID(ID.Type.ConstantUID, "u1234567890"))),
            evaluate("C{u1234567890}"))
    }

    @Test
    fun testProgramDataElement() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.PROGRAM_DATA_ELEMENT,
                    ID(ID.Type.ProgramUID, "u1234567890"),
                    ID(ID.Type.DataElementUID, "v1234567890"))),
            evaluate("D{u1234567890.v1234567890}"))
    }

    @Test
    fun testProgramIndicator() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.PROGRAM_INDICATOR,
                    ID(ID.Type.ProgramIndicatorUID, "u1234567890"))),
            evaluate("I{u1234567890}"))
    }

    @Test
    fun testIndicator() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.INDICATOR,
                    ID(ID.Type.IndicatorUID, "u1234567890"))),
            evaluate("N{u1234567890}"))
    }

    @Test
    fun testOrganisationUnitGroup() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.ORG_UNIT_GROUP,
                    ID(ID.Type.OrganisationUnitGroupUID, "u1234567890"))),
            evaluate("OUG{u1234567890}"))
    }

    @Test
    fun testDataSet() {
        assertEquals(
            setOf(
                DataItem(
                    DataItemType.REPORTING_RATE,
                    ID(ID.Type.DataSetUID, "u1234567890"), ID(ID.Type.ReportingRateType, "ACTUAL_REPORTS"))),
            evaluate("R{u1234567890.ACTUAL_REPORTS}"))
    }

    companion object {
        private fun evaluate(expression: String): Set<DataItem> {
            return Expression(expression, Expression.Mode.INDICATOR_EXPRESSION).collectDataItems()
        }
    }
}
