package org.hisp.dhis.expression;

import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.parse.ExprGrammar;
import org.hisp.dhis.expression.parse.FragmentContext;
import org.hisp.dhis.expression.parse.NamedFragments;
import org.hisp.dhis.expression.parse.Parser;
import org.hisp.dhis.expression.spi.DataItem;
import org.hisp.dhis.expression.spi.DataItemModifiers;
import org.hisp.dhis.expression.spi.UID;
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
import static org.hisp.dhis.expression.spi.UID.Type.Attribute;
import static org.hisp.dhis.expression.spi.UID.Type.AttributeOptionCombo;
import static org.hisp.dhis.expression.spi.UID.Type.CategoryOption;
import static org.hisp.dhis.expression.spi.UID.Type.CategoryOptionGroup;
import static org.hisp.dhis.expression.spi.UID.Type.Constant;
import static org.hisp.dhis.expression.spi.UID.Type.DataElement;
import static org.hisp.dhis.expression.spi.UID.Type.DataSet;
import static org.hisp.dhis.expression.spi.UID.Type.DateElementGroup;
import static org.hisp.dhis.expression.spi.UID.Type.Indicator;
import static org.hisp.dhis.expression.spi.UID.Type.OrganisationUnitGroup;
import static org.hisp.dhis.expression.spi.UID.Type.Program;
import static org.hisp.dhis.expression.spi.UID.Type.ProgramIndicator;
import static org.hisp.dhis.expression.spi.UID.Type.ProgramStage;
import static org.hisp.dhis.expression.spi.UID.Type.ProgramVariable;
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
                        new UID(DataElement, "u1234567890"))),
                evaluate("#{u1234567890}"));
    }

    @Test
    void testDataElement2() {
        assertEquals(Set.of(new DataItem(DATA_ELEMENT,
                                new UID(DataElement, "u1234567890")),
                        new DataItem(DATA_ELEMENT,
                                new UID(DataElement, "v1234567890"))),
                evaluate("#{u1234567890} + #{v1234567890}"));
    }

    @Test
    void testDataElementGroup() {
        assertEquals(Set.of(new DataItem(DATA_ELEMENT,
                        new UID(DateElementGroup, "u1234567890"))),
                evaluate("#{deGroup:u1234567890}"));
    }

    @Test
    void testProgramStageDataElement() {
        assertEquals(Set.of(new DataItem(DATA_ELEMENT,
                        new UID(ProgramStage, "u1234567890"),
                        new UID(DataElement, "v1234567890"))),
                evaluate("#{u1234567890.v1234567890}"));
    }

    @Test
    void testProgramStageCategoryOption() {
        assertEquals(Set.of(new DataItem(DATA_ELEMENT,
                        new UID(ProgramStage, "u1234567890"),
                        new UID(CategoryOption, "v1234567890"))),
                evaluate("#{u1234567890.co:v1234567890}"));
    }

    @Test
    void testProgramStageCategoryOptionGroup() {
        assertEquals(Set.of(new DataItem(DATA_ELEMENT,
                        new UID(ProgramStage, "u1234567890"),
                        List.of(new UID(CategoryOptionGroup, "v1234567890"), new UID(CategoryOptionGroup, "w1234567890")),
                        List.of(),
                        DataItemModifiers.builder().build())),
                evaluate("#{u1234567890.coGroup:v1234567890&w1234567890}"));
    }

    @Test
    void testDataElementCategoryOptionAttributeOptionCombo() {
        assertEquals(Set.of(new DataItem(DATA_ELEMENT,
                        new UID(DataElement, "u1234567890"),
                        new UID(CategoryOption, "v1234567890"),
                        new UID(AttributeOptionCombo, "w1234567890"))),
                evaluate("#{u1234567890.v1234567890.w1234567890}"));
    }

    @Test
    void testAttribute() {
        assertEquals(Set.of(new DataItem(ATTRIBUTE,
                        new UID(Attribute, "u1234567890"))),
                evaluate("A{u1234567890}"));
    }

    @Test
    void testProgramAttribute() {
        assertEquals(Set.of(new DataItem(ATTRIBUTE,
                        new UID(Program, "u1234567890"),
                        new UID(Attribute, "v1234567890"))),
                evaluate("A{u1234567890.v1234567890}"));
    }

    @Test
    void testConstant() {
        assertEquals(Set.of(new DataItem(CONSTANT,
                        new UID(Constant, "u1234567890"))),
                evaluate("C{u1234567890}"));
    }

    @Test
    void testProgramDataElement() {
        assertEquals(Set.of(new DataItem(PROGRAM_DATA_ELEMENT,
                        new UID(Program, "u1234567890"),
                        new UID(DataElement, "v1234567890"))),
                evaluate("D{u1234567890.v1234567890}"));
    }

    @Test
    void testProgramIndicator() {
        assertEquals(Set.of(new DataItem(PROGRAM_INDICATOR,
                        new UID(ProgramIndicator, "u1234567890"))),
                evaluate("I{u1234567890}"));
    }

    @Test
    void testIndicator() {
        assertEquals(Set.of(new DataItem(INDICATOR,
                        new UID(Indicator, "u1234567890"))),
                evaluate("N{u1234567890}"));
    }

    @Test
    void testOrganisationUnitGroup() {
        assertEquals(Set.of(new DataItem(ORG_UNIT_GROUP,
                        new UID(OrganisationUnitGroup, "u1234567890"))),
                evaluate("OUG{u1234567890}"));
    }

    @Test
    void testDataSet() {
        //FIXME convert reporting rate type to a UID value? or some other thing?
        assertEquals(Set.of(new DataItem(REPORTING_RATE,
                        new UID(DataSet, "u1234567890"))),
                evaluate("R{u1234567890.ACTUAL_REPORTS}"));
    }

    @Test
    void testProgramVariable() {
        //FIXME a PV is not a data item - what to do?
        assertEquals(Set.of(new DataItem(PROGRAM_VARIABLE,
                        new UID(ProgramVariable, "u1234567890"))),
                evaluate("V{u1234567890}"));
    }

    private static final NamedFragments FRAGMENTS = new FragmentContext(ExprGrammar.Constants, ExprGrammar.Functions, ExprGrammar.Modifiers);

    private Set<DataItem> evaluate(String expression) {
        Node<?> root = Parser.parse(expression, FRAGMENTS);
        Node.attachModifiers(root);
        Node.groupOperators(root);
        return Node.collectDataItems(root);
    }
}
