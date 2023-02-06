package org.hisp.dhis.lib.expression.spi;

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
            List.of(ID.Type.DataElementUID),
            List.of(ID.Type.ProgramStageUID, ID.Type.DataElementUID),
            List.of(ID.Type.DataElementUID, ID.Type.CategoryOptionUID, ID.Type.AttributeOptionComboUID))),
    // (Program Attribute, not currently used for aggregate data)
    ATTRIBUTE("A", List.of(
            List.of(ID.Type.AttributeUID),
            List.of(ID.Type.ProgramUID, ID.Type.AttributeUID))),
    CONSTANT("C", ID.Type.ConstantUID),
    // (Not to be confused with #, also used in programs)
    PROGRAM_DATA_ELEMENT("D", ID.Type.ProgramUID, ID.Type.DataElementUID),
    PROGRAM_INDICATOR("I", ID.Type.ProgramIndicatorUID),
    // ('I' was already taken for program indicator)
    INDICATOR("N", ID.Type.IndicatorUID),
    ORG_UNIT_GROUP("OUG", ID.Type.OrganisationUnitGroupUID),
    REPORTING_RATE("R", ID.Type.DataSetUID, ID.Type.ReportingRateType),
    PROGRAM_VARIABLE("V", ID.Type.ProgramVariableName);

    private final String symbol;
    private final List<List<ID.Type>> parameterTypes;

    DataItemType(String symbol, ID.Type... parameterTypes) {
        this(symbol, List.of(List.of(parameterTypes)));
    }
    DataItemType(String symbol, List<List<ID.Type>> parameterTypes) {
        this.symbol = symbol;
        this.parameterTypes = parameterTypes;
    }

    public String getSymbol() {
        return symbol;
    }

    public ID.Type getType(int numberOfIds, int index) {
        List<ID.Type> params = parameterTypes.stream().filter(l -> l.size() == numberOfIds).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(format("Data item %s cannot be used with %d ids", name(), numberOfIds)));
        return params.get(index);
    }

    /**
     * Avoid defensive copy when finding operator by symbol
     */
    private static final List<DataItemType> VALUES = List.of(values());

    public static DataItemType fromSymbol(String symbol) {
        return VALUES.stream().filter(op -> op.symbol.equals(symbol)).findFirst().orElseThrow();
    }
}
