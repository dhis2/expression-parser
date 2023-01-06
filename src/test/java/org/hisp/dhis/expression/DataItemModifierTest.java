package org.hisp.dhis.expression;

import org.hisp.dhis.expression.spi.DataItem;
import org.hisp.dhis.expression.spi.DataItemModifiers;
import org.hisp.dhis.expression.spi.ID;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.hisp.dhis.expression.ast.AggregationType.sum;
import static org.hisp.dhis.expression.spi.DataItemType.DATA_ELEMENT;
import static org.hisp.dhis.expression.spi.ID.Type.DataElementUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link DataItem} which have non-standard {@link org.hisp.dhis.expression.spi.DataItemModifiers}.
 *
 * @author Jan Bernitt
 */
class DataItemModifierTest {

    /**
     * Any data item within an aggregation function is a period aggregation.
     * The periods themselves are implicit and do not occur anywhere in the expression.
     */
    @Test
    void testPeriodAggregation() {
        DataItemModifiers expected = DataItemModifiers.builder().periodAggregation(true).build();
        assertEquals(Set.of(new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected)),
                evaluate("avg(#{u1234567890})"));
    }

    @Test
    void testAggregationType() {
        DataItemModifiers expected = DataItemModifiers.builder().aggregationType(sum).build();
        assertEquals(Set.of(new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected)),
                evaluate("#{u1234567890}.aggregationType(sum)"));
    }

    @Test
    void testMaxDate() {
        DataItemModifiers expected = DataItemModifiers.builder().maxDate(LocalDate.parse("1980-11-11")).build();
        assertEquals(Set.of(new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected)),
                evaluate("#{u1234567890}.maxDate(1980-11-11)"));
    }

    @Test
    void testMinDate() {
        DataItemModifiers expected = DataItemModifiers.builder().minDate(LocalDate.parse("1980-11-11")).build();
        assertEquals(Set.of(new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected)),
                evaluate("#{u1234567890}.minDate(1980-11-11)"));
    }

    @Test
    void testPeriodOffset() {
        DataItemModifiers expected = DataItemModifiers.builder().periodOffset(42).build();
        assertEquals(Set.of(new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected)),
                evaluate("#{u1234567890}.periodOffset(42)"));
    }

    @Test
    void testStageOffset() {
        DataItemModifiers expected = DataItemModifiers.builder().stageOffset(42).build();
        assertEquals(Set.of(new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected)),
                evaluate("#{u1234567890}.stageOffset(42)"));
    }

    @Test
    void testYearToDate() {
        DataItemModifiers expected = DataItemModifiers.builder().yearToDate(true).build();
        assertEquals(Set.of(new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected)),
                evaluate("#{u1234567890}.yearToDate()"));
    }

    /*
    Combinations
     */

    @Test
    void testMultipleModifiersDirectlyApplied() {
        DataItemModifiers expected = DataItemModifiers.builder().yearToDate(true).stageOffset(42).build();
        assertEquals(Set.of(new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected)),
                evaluate("#{u1234567890}.yearToDate().stageOffset(42)"));
    }

    @Test
    void testMultipleModifiersIndirectlyApplied() {
        DataItemModifiers expected = DataItemModifiers.builder().yearToDate(true).stageOffset(42).build();
        assertEquals(Set.of(new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected)),
                evaluate("d2:ceil(#{u1234567890}).yearToDate().stageOffset(42)"));
    }

    @Test
    void testMultipleModifiersIndirectlyAppliedToMany() {
        DataItemModifiers expected = DataItemModifiers.builder().yearToDate(true).stageOffset(42).build();
        assertEquals(Set.of(
                    new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected),
                    new DataItem(DATA_ELEMENT, new ID(DataElementUID, "v1234567890"), expected)),
                evaluate("d2:ceil(#{u1234567890} + #{v1234567890}).yearToDate().stageOffset(42)"));
    }

    @Test
    void testMultipleModifiersIndirectlyAppliedToManyWithAggregation() {
        DataItemModifiers expected = DataItemModifiers.builder().yearToDate(true).stageOffset(42).periodAggregation(true).build();
        assertEquals(Set.of(
                    new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected),
                    new DataItem(DATA_ELEMENT, new ID(DataElementUID, "v1234567890"), expected)),
                evaluate("avg(#{u1234567890} + #{v1234567890}).yearToDate().stageOffset(42)"));
    }

    @Test
    void testMultipleModifiersIndirectlyAppliedToManyWithAggregationAndIndividualDifferences() {
        DataItemModifiers expected1 = DataItemModifiers.builder().yearToDate(true).stageOffset(42).periodAggregation(true).build();
        DataItemModifiers expected2 = DataItemModifiers.builder().stageOffset(42).periodAggregation(true).build();
        assertEquals(Set.of(
                        new DataItem(DATA_ELEMENT, new ID(DataElementUID, "u1234567890"), expected1),
                        new DataItem(DATA_ELEMENT, new ID(DataElementUID, "v1234567890"), expected2)),
                evaluate("avg(#{u1234567890}.yearToDate() + #{v1234567890}).stageOffset(42)"));
    }

    private static Set<DataItem> evaluate(String expression) {
        return new Expression(expression).collectDataItems();
    }
}
