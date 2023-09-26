package org.hisp.dhis.lib.expression

import org.hisp.dhis.lib.expression.ast.AggregationType
import org.hisp.dhis.lib.expression.spi.DataItem
import org.hisp.dhis.lib.expression.spi.DataItemType
import org.hisp.dhis.lib.expression.spi.ID
import org.hisp.dhis.lib.expression.spi.QueryModifiers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * Tests [DataItem] which have non-standard [QueryModifiers].
 *
 * @author Jan Bernitt
 */
internal class DataItemModifierExpressionTest {
    /**
     * Any data item within an aggregation function is a period aggregation. The periods themselves are implicit and do
     * not occur anywhere in the expression.
     */
    @Test
    fun testPeriodAggregation() {
        val expected: QueryModifiers = QueryModifiers().copy(periodAggregation = true)
        assertEquals(
            setOf(DataItem(DataItemType.DATA_ELEMENT, ID(ID.Type.DataElementUID, "u1234567890"), expected)),
            evaluate("avg(#{u1234567890})", Expression.Mode.PREDICTOR_GENERATOR_EXPRESSION))
    }

    @Test
    fun testAggregationType() {
        val expected: QueryModifiers = QueryModifiers().copy(aggregationType = AggregationType.sum)
        assertEquals(
            setOf(DataItem(DataItemType.DATA_ELEMENT, ID(ID.Type.DataElementUID, "u1234567890"), expected)),
            evaluate("#{u1234567890}.aggregationType(sum)", Expression.Mode.INDICATOR_EXPRESSION))
    }

    @Test
    fun testMaxDate() {
        val expected: QueryModifiers = QueryModifiers().copy(maxDate = LocalDate.parse("1980-11-11"))
        assertEquals(
            setOf(DataItem(DataItemType.DATA_ELEMENT, ID(ID.Type.DataElementUID, "u1234567890"), expected)),
            evaluate("#{u1234567890}.maxDate(1980-11-11)", Expression.Mode.PREDICTOR_GENERATOR_EXPRESSION))
    }

    @Test
    fun testMinDate() {
        val expected: QueryModifiers = QueryModifiers().copy(minDate = LocalDate.parse("1980-11-11"))
        assertEquals(
            setOf(DataItem(DataItemType.DATA_ELEMENT, ID(ID.Type.DataElementUID, "u1234567890"), expected)),
            evaluate("#{u1234567890}.minDate(1980-11-11)", Expression.Mode.PREDICTOR_GENERATOR_EXPRESSION))
    }

    @Test
    fun testPeriodOffset() {
        val expected: QueryModifiers = QueryModifiers().copy(periodOffset = 42)
        assertEquals(
            setOf(DataItem(DataItemType.DATA_ELEMENT, ID(ID.Type.DataElementUID, "u1234567890"), expected)),
            evaluate("#{u1234567890}.periodOffset(42)", Expression.Mode.INDICATOR_EXPRESSION))
    }

    @Test
    fun testStageOffset() {
        val expected: QueryModifiers = QueryModifiers().copy(stageOffset = 42)
        assertEquals(
            setOf(DataItem(DataItemType.DATA_ELEMENT, ID(ID.Type.DataElementUID, "u1234567890"), expected)),
            evaluate("#{u1234567890}.stageOffset(42)", Expression.Mode.PROGRAM_INDICATOR_EXPRESSION))
    }

    @Test
    fun testYearToDate() {
        val expected: QueryModifiers = QueryModifiers().copy( yearToDate = true)
        assertEquals(
            setOf(DataItem(DataItemType.DATA_ELEMENT, ID(ID.Type.DataElementUID, "u1234567890"), expected)),
            evaluate("#{u1234567890}.yearToDate()", Expression.Mode.INDICATOR_EXPRESSION))
    }

    /*
    Combinations
     */
    @Test
    fun testMultipleModifiersDirectlyApplied() {
        val expected: QueryModifiers = QueryModifiers().copy(yearToDate = true, periodOffset = 42)
        assertEquals(
            setOf(DataItem(DataItemType.DATA_ELEMENT, ID(ID.Type.DataElementUID, "u1234567890"), expected)),
            evaluate("#{u1234567890}.yearToDate().periodOffset(42)", Expression.Mode.INDICATOR_EXPRESSION))
    }

    @Test
    fun testMultipleModifiersIndirectlyApplied() {
        val expected: QueryModifiers = QueryModifiers().copy(yearToDate = true, periodOffset = 42)
        assertEquals(
            setOf(DataItem(DataItemType.DATA_ELEMENT, ID(ID.Type.DataElementUID, "u1234567890"), expected)),
            evaluate("(#{u1234567890}).yearToDate().periodOffset(42)", Expression.Mode.INDICATOR_EXPRESSION))
    }

    @Test
    fun testMultipleModifiersIndirectlyAppliedToMany() {
        val expected: QueryModifiers = QueryModifiers().copy(yearToDate = true, periodOffset = 42)
        assertEquals(
            setOf(
                DataItem(DataItemType.DATA_ELEMENT, ID(ID.Type.DataElementUID, "u1234567890"), expected),
                DataItem(DataItemType.DATA_ELEMENT, ID(ID.Type.DataElementUID, "v1234567890"), expected)),
            evaluate(
                "(#{u1234567890} + #{v1234567890}).yearToDate().periodOffset(42)",
                Expression.Mode.INDICATOR_EXPRESSION))
    }

    @Test
    fun testMultipleModifiersIndirectlyAppliedToManyWithAggregation() {
        val expected: QueryModifiers = QueryModifiers().copy(
            minDate = LocalDate.parse("1980-11-11"),
            maxDate = LocalDate.parse("2000-11-11"),
            periodAggregation = true)
        assertEquals(
            setOf(
                DataItem(DataItemType.DATA_ELEMENT, ID(ID.Type.DataElementUID, "u1234567890"), expected),
                DataItem(DataItemType.DATA_ELEMENT, ID(ID.Type.DataElementUID, "v1234567890"), expected)),
            evaluate(
                "avg(#{u1234567890} + #{v1234567890}).minDate(1980-11-11).maxDate(2000-11-11)",
                Expression.Mode.PREDICTOR_GENERATOR_EXPRESSION))
    }

    @Test
    fun testMultipleModifiersIndirectlyAppliedToManyWithAggregationAndIndividualDifferences() {
        val expected1: QueryModifiers = QueryModifiers().copy(yearToDate = true, periodOffset = 42)
        val expected2: QueryModifiers = QueryModifiers().copy(periodOffset = 42)
        assertEquals(
            setOf(
                DataItem(DataItemType.DATA_ELEMENT, ID(ID.Type.DataElementUID, "u1234567890"), expected1),
                DataItem(DataItemType.DATA_ELEMENT, ID(ID.Type.DataElementUID, "v1234567890"), expected2)),
            evaluate(
                "(#{u1234567890}.yearToDate() + #{v1234567890}).periodOffset(42)",
                Expression.Mode.INDICATOR_EXPRESSION))
    }

    companion object {
        private fun evaluate(expression: String, mode: Expression.Mode): Set<DataItem> {
            val expr = Expression(expression, mode)
            return expr.collectDataItems()
        }
    }
}
