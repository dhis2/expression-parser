package org.hisp.dhis.expression;

import java.util.List;

/**
 * The different types of data value references in the expression grammar.
 *
 * @author Jan Bernitt
 */
public enum DataValue
{
    DATA_ELEMENT("#"), // (data element for aggregate vs. program stage . data element for programs)
    ATTRIBUTE("A"), // (Program Attribute, not currently used for aggregate data)
    CONSTANT("C"),
    PROGRAM_DATA_ELEMENT("D"), // (Not to be confused with #, also used in programs)
    PROGRAM_INDICATOR("I"),
    INDICATOR("N"), // ('I' was already taken for program indicator)
    ORG_UNIT_GROUP("OUG"),
    REPORTING_RATE("R"),
    PROGRAM_VARIABLE("V");

    private final String symbol;

    DataValue(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    /**
     * Avoid defensive copy when finding operator by symbol
     */
    private static final List<DataValue> VALUES = List.of(values());

    static DataValue fromSymbol(String symbol) {
        return VALUES.stream().filter(op -> op.symbol.equals(symbol)).findFirst().orElseThrow();
    }
}
