package org.hisp.dhis.lib.expression;

import org.hisp.dhis.lib.expression.spi.DataItem;
import org.hisp.dhis.lib.expression.spi.ID;
import org.hisp.dhis.lib.expression.spi.QueryModifiers;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.hisp.dhis.lib.expression.Expression.Mode.*;
import static org.hisp.dhis.lib.expression.ast.AggregationType.sum;
import static org.hisp.dhis.lib.expression.spi.DataItemType.DATA_ELEMENT;
import static org.hisp.dhis.lib.expression.spi.ID.Type.DataElementUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link DataItem} which have non-standard {@link QueryModifiers}.
 *
 * @author Jan Bernitt
 */
class DataItemModifierExpressionTest {

    /**
     * Any data item within an aggregation function is a period aggregation. The periods themselves are implicit and do
     * not occur anywhere in the expression.
     */
    @Test
    void testPeriodAggregation() {
        QueryModifiers expected = QueryModifiers.builder().periodAggregation(true).build();
        assertEquals(Set.of(new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected)),
                evaluate("avg(#{u1234567890})", PREDICTOR_GENERATOR_EXPRESSION));
    }

    @Test
    void testAggregationType() {
        QueryModifiers expected = QueryModifiers.builder().aggregationType(sum).build();
        assertEquals(Set.of(new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected)),
                evaluate("#{u1234567890}.aggregationType(sum)", INDICATOR_EXPRESSION));
    }

    @Test
    void testMaxDate() {
        QueryModifiers expected = QueryModifiers.builder().maxDate(LocalDate.parse("1980-11-11")).build();
        assertEquals(Set.of(new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected)),
                evaluate("#{u1234567890}.maxDate(1980-11-11)", PREDICTOR_GENERATOR_EXPRESSION));
    }

    @Test
    void testMinDate() {
        QueryModifiers expected = QueryModifiers.builder().minDate(LocalDate.parse("1980-11-11")).build();
        assertEquals(Set.of(new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected)),
                evaluate("#{u1234567890}.minDate(1980-11-11)", PREDICTOR_GENERATOR_EXPRESSION));
    }

    @Test
    void testPeriodOffset() {
        QueryModifiers expected = QueryModifiers.builder().periodOffset(42).build();
        assertEquals(Set.of(new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected)),
                evaluate("#{u1234567890}.periodOffset(42)", INDICATOR_EXPRESSION));
    }

    @Test
    void testStageOffset() {
        QueryModifiers expected = QueryModifiers.builder().stageOffset(42).build();
        assertEquals(Set.of(new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected)),
                evaluate("#{u1234567890}.stageOffset(42)", PROGRAM_INDICATOR_EXPRESSION));
    }

    @Test
    void testYearToDate() {
        QueryModifiers expected = QueryModifiers.builder().yearToDate(true).build();
        assertEquals(Set.of(new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected)),
                evaluate("#{u1234567890}.yearToDate()", INDICATOR_EXPRESSION));
    }

    /*
    Combinations
     */

    @Test
    void testMultipleModifiersDirectlyApplied() {
        QueryModifiers expected = QueryModifiers.builder().yearToDate(true).periodOffset(42).build();
        assertEquals(Set.of(new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected)),
                evaluate("#{u1234567890}.yearToDate().periodOffset(42)", INDICATOR_EXPRESSION));
    }

    @Test
    void testMultipleModifiersIndirectlyApplied() {
        QueryModifiers expected = QueryModifiers.builder().yearToDate(true).periodOffset(42).build();
        assertEquals(Set.of(new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected)),
                evaluate("(#{u1234567890}).yearToDate().periodOffset(42)", INDICATOR_EXPRESSION));
    }

    @Test
    void testMultipleModifiersIndirectlyAppliedToMany() {
        QueryModifiers expected = QueryModifiers.builder().yearToDate(true).periodOffset(42).build();
        assertEquals(Set.of(
                        new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected),
                        new DataItem(DATA_ELEMENT, new ID(DataElementUID, "v1234567890"), expected)),
                evaluate("(#{u1234567890} + #{v1234567890}).yearToDate().periodOffset(42)", INDICATOR_EXPRESSION));
    }

    @Test
    void testMultipleModifiersIndirectlyAppliedToManyWithAggregation() {
        QueryModifiers expected = QueryModifiers.builder()
                .minDate(LocalDate.parse("1980-11-11"))
                .maxDate(LocalDate.parse("2000-11-11"))
                .periodAggregation(true).build();
        assertEquals(Set.of(
                        new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected),
                        new DataItem(DATA_ELEMENT, new ID(DataElementUID, "v1234567890"), expected)),
                evaluate("avg(#{u1234567890} + #{v1234567890}).minDate(1980-11-11).maxDate(2000-11-11)", PREDICTOR_GENERATOR_EXPRESSION));
    }

    @Test
    void testMultipleModifiersIndirectlyAppliedToManyWithAggregationAndIndividualDifferences() {
        QueryModifiers expected1 = QueryModifiers.builder().yearToDate(true).periodOffset(42).build();
        QueryModifiers expected2 = QueryModifiers.builder().periodOffset(42).build();
        assertEquals(Set.of(
                        new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected1),
                        new DataItem(DATA_ELEMENT, new ID(DataElementUID, "v1234567890"), expected2)),
                evaluate("(#{u1234567890}.yearToDate() + #{v1234567890}).periodOffset(42)", INDICATOR_EXPRESSION));
    }

    private static Set<DataItem> evaluate(String expression, Expression.Mode mode) {
        Expression expr = new Expression(expression, mode);
        return expr.collectDataItems();
    }
}
