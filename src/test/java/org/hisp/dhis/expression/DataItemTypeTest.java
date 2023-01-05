package org.hisp.dhis.expression;

import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.syntax.ExpressionGrammar;
import org.hisp.dhis.expression.syntax.Parser;
import org.hisp.dhis.expression.spi.DataItem;
import org.hisp.dhis.expression.spi.DataItemModifiers;
import org.hisp.dhis.expression.spi.ID;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.hisp.dhis.expression.spi.DataItemType.ATTRIBUTE;
import static org.hisp.dhis.expression.spi.DataItemType.CONSTANT;
import static org.hisp.dhis.expression.spi.DataItemType.DATA_ELEMENT;
import static org.hisp.dhis.expression.spi.DataItemType.INDICATOR;
import static org.hisp.dhis.expression.spi.DataItemType.ORG_UNIT_GROUP;
import static org.hisp.dhis.expression.spi.DataItemType.PROGRAM_DATA_ELEMENT;
import static org.hisp.dhis.expression.spi.DataItemType.PROGRAM_INDICATOR;
import static org.hisp.dhis.expression.spi.DataItemType.PROGRAM_VARIABLE;
import static org.hisp.dhis.expression.spi.DataItemType.REPORTING_RATE;
import static org.hisp.dhis.expression.spi.ID.Type.AttributeUID;
import static org.hisp.dhis.expression.spi.ID.Type.AttributeOptionComboUID;
import static org.hisp.dhis.expression.spi.ID.Type.CategoryOptionUID;
import static org.hisp.dhis.expression.spi.ID.Type.CategoryOptionGroupUID;
import static org.hisp.dhis.expression.spi.ID.Type.ConstantUID;
import static org.hisp.dhis.expression.spi.ID.Type.DataElementUID;
import static org.hisp.dhis.expression.spi.ID.Type.DataSetUID;
import static org.hisp.dhis.expression.spi.ID.Type.DateElementGroupUID;
import static org.hisp.dhis.expression.spi.ID.Type.IndicatorUID;
import static org.hisp.dhis.expression.spi.ID.Type.OrganisationUnitGroupUID;
import static org.hisp.dhis.expression.spi.ID.Type.ProgramUID;
import static org.hisp.dhis.expression.spi.ID.Type.ProgramIndicatorUID;
import static org.hisp.dhis.expression.spi.ID.Type.ProgramStageUID;
import static org.hisp.dhis.expression.spi.ID.Type.ProgramVariableName;
import static org.hisp.dhis.expression.spi.ID.Type.ReportingRateType;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests all forms of {@link DataItem}s are correctly transformed from an expression to an AST and into a {@link DataItem} value.
 *
 * @author Jan Bernitt
 */
class DataItemTypeTest {

    @Test
    void testDataElement() {
        assertEquals(Set.of(new DataItem(DATA_ELEMENT,
                        new ID(DataElementUID, "u1234567890"))),
                evaluate("#{u1234567890}"));
    }

    @Test
    void testDataElement2() {
        assertEquals(Set.of(new DataItem(DATA_ELEMENT,
                                new ID(DataElementUID, "u1234567890")),
                        new DataItem(DATA_ELEMENT,
                                new ID(DataElementUID, "v1234567890"))),
                evaluate("#{u1234567890} + #{v1234567890}"));
    }

    @Test
    void testDataElementGroup() {
        assertEquals(Set.of(new DataItem(DATA_ELEMENT,
                        new ID(DateElementGroupUID, "u1234567890"))),
                evaluate("#{deGroup:u1234567890}"));
    }

    @Test
    void testProgramStageDataElement() {
        assertEquals(Set.of(new DataItem(DATA_ELEMENT,
                        new ID(ProgramStageUID, "u1234567890"),
                        new ID(DataElementUID, "v1234567890"))),
                evaluate("#{u1234567890.v1234567890}"));
    }

    @Test
    void testProgramStageCategoryOption() {
        assertEquals(Set.of(new DataItem(DATA_ELEMENT,
                        new ID(ProgramStageUID, "u1234567890"),
                        new ID(CategoryOptionUID, "v1234567890"))),
                evaluate("#{u1234567890.co:v1234567890}"));
    }

    @Test
    void testProgramStageCategoryOptionGroup() {
        assertEquals(Set.of(new DataItem(DATA_ELEMENT,
                        new ID(ProgramStageUID, "u1234567890"),
                        List.of(new ID(CategoryOptionGroupUID, "v1234567890"), new ID(CategoryOptionGroupUID, "w1234567890")),
                        List.of(),
                        DataItemModifiers.builder().build())),
                evaluate("#{u1234567890.coGroup:v1234567890&w1234567890}"));
    }

    @Test
    void testDataElementCategoryOptionAttributeOptionCombo() {
        assertEquals(Set.of(new DataItem(DATA_ELEMENT,
                        new ID(DataElementUID, "u1234567890"),
                        new ID(CategoryOptionUID, "v1234567890"),
                        new ID(AttributeOptionComboUID, "w1234567890"))),
                evaluate("#{u1234567890.v1234567890.w1234567890}"));
    }

    @Test
    void testAttribute() {
        assertEquals(Set.of(new DataItem(ATTRIBUTE,
                        new ID(AttributeUID, "u1234567890"))),
                evaluate("A{u1234567890}"));
    }

    @Test
    void testProgramAttribute() {
        assertEquals(Set.of(new DataItem(ATTRIBUTE,
                        new ID(ProgramUID, "u1234567890"),
                        new ID(AttributeUID, "v1234567890"))),
                evaluate("A{u1234567890.v1234567890}"));
    }

    @Test
    void testConstant() {
        assertEquals(Set.of(new DataItem(CONSTANT,
                        new ID(ConstantUID, "u1234567890"))),
                evaluate("C{u1234567890}"));
    }

    @Test
    void testProgramDataElement() {
        assertEquals(Set.of(new DataItem(PROGRAM_DATA_ELEMENT,
                        new ID(ProgramUID, "u1234567890"),
                        new ID(DataElementUID, "v1234567890"))),
                evaluate("D{u1234567890.v1234567890}"));
    }

    @Test
    void testProgramIndicator() {
        assertEquals(Set.of(new DataItem(PROGRAM_INDICATOR,
                        new ID(ProgramIndicatorUID, "u1234567890"))),
                evaluate("I{u1234567890}"));
    }

    @Test
    void testIndicator() {
        assertEquals(Set.of(new DataItem(INDICATOR,
                        new ID(IndicatorUID, "u1234567890"))),
                evaluate("N{u1234567890}"));
    }

    @Test
    void testOrganisationUnitGroup() {
        assertEquals(Set.of(new DataItem(ORG_UNIT_GROUP,
                        new ID(OrganisationUnitGroupUID, "u1234567890"))),
                evaluate("OUG{u1234567890}"));
    }

    @Test
    void testDataSet() {
        assertEquals(Set.of(new DataItem(REPORTING_RATE,
                        new ID(DataSetUID, "u1234567890"), new ID(ReportingRateType, "ACTUAL_REPORTS"))),
                evaluate("R{u1234567890.ACTUAL_REPORTS}"));
    }

    private static Set<DataItem> evaluate(String expression) {
        return Node.collectDataItems(Parser.parse(expression, ExpressionGrammar.Fragments));
    }
}
