package org.hisp.dhis.expression.ast;

import java.util.List;

import static java.lang.String.format;

/**
 * The different types of data value references in the expression grammar.
 *
 * @author Jan Bernitt
 */
public enum DataItemType
{
    // (data element for aggregate vs. program stage . data element for programs)
    DATA_ELEMENT("#", List.of(
            List.of(UID.Type.DataElement),
            List.of(UID.Type.ProgramStage, UID.Type.DataElement),
            List.of(UID.Type.DataElement , UID.Type.CategoryOption, UID.Type.AttributeOptionCombo))),
    // (Program Attribute, not currently used for aggregate data)
    ATTRIBUTE("A", List.of(
            List.of(UID.Type.Attribute),
            List.of(UID.Type.Program, UID.Type.Attribute))),
    CONSTANT("C", UID.Type.Constant),
    // (Not to be confused with #, also used in programs)
    PROGRAM_DATA_ELEMENT("D", UID.Type.Program, UID.Type.DataElement),
    PROGRAM_INDICATOR("I", UID.Type.ProgramIndicator),
    // ('I' was already taken for program indicator)
    INDICATOR("N", UID.Type.Indicator),
    ORG_UNIT_GROUP("OUG", UID.Type.OrganisationUnitGroup),
    REPORTING_RATE("R", UID.Type.DataSet),
    PROGRAM_VARIABLE("V", UID.Type.ProgramVariable);

    private final String symbol;
    private final List<List<UID.Type>> parameterTypes;

    DataItemType(String symbol, UID.Type... parameterTypes) {
        this(symbol, List.of(List.of(parameterTypes)));
    }
    DataItemType(String symbol, List<List<UID.Type>> parameterTypes) {
        this.symbol = symbol;
        this.parameterTypes = parameterTypes;
    }

    public String getSymbol() {
        return symbol;
    }

    public UID.Type getType(int numberOfIds, int index) {
        List<UID.Type> params = parameterTypes.stream().filter(l -> l.size() == numberOfIds).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(format("Data item %s cannot be used with %d ids", name(), numberOfIds)));
        return params.get(index);
    }

    /**
     * Avoid defensive copy when finding operator by symbol
     */
    private static final List<DataItemType> VALUES = List.of(values());

    static DataItemType fromSymbol(String symbol) {
        return VALUES.stream().filter(op -> op.symbol.equals(symbol)).findFirst().orElseThrow();
    }
}
