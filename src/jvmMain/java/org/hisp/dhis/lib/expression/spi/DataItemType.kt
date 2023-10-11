package org.hisp.dhis.lib.expression.spi

/**
 * The different types of data value references in the expression grammar.
 *
 * @author Jan Bernitt
 */
enum class DataItemType(val symbol: String, private val parameterTypes: List<List<ID.Type>>) {
    // (data element for aggregate vs. program stage . data element for programs)
    DATA_ELEMENT(
        "#", listOf(
            listOf(ID.Type.DataElementUID),
            listOf(ID.Type.ProgramStageUID, ID.Type.DataElementUID),
            listOf(ID.Type.DataElementUID, ID.Type.CategoryOptionUID, ID.Type.AttributeOptionComboUID)
        )
    ),

    // (Program Attribute, not currently used for aggregate data)
    ATTRIBUTE(
        "A", listOf(
            listOf(ID.Type.AttributeUID),
            listOf(ID.Type.ProgramUID, ID.Type.AttributeUID)
        )
    ),
    CONSTANT("C", ID.Type.ConstantUID),

    // (Not to be confused with #, also used in programs)
    PROGRAM_DATA_ELEMENT("D", ID.Type.ProgramUID, ID.Type.DataElementUID),
    PROGRAM_INDICATOR("I", ID.Type.ProgramIndicatorUID),

    // ('I' was already taken for program indicator)
    INDICATOR("N", ID.Type.IndicatorUID),
    ORG_UNIT_GROUP("OUG", ID.Type.OrganisationUnitGroupUID),
    REPORTING_RATE("R", ID.Type.DataSetUID, ID.Type.ReportingRateType),
    PROGRAM_VARIABLE("V", ID.Type.ProgramVariableName);

    constructor(symbol: String, vararg parameterTypes: ID.Type) : this(
        symbol,
        listOf<List<ID.Type>>(listOf<ID.Type>(*parameterTypes))
    )

    fun getType(numberOfIds: Int, index: Int): ID.Type {
        val params =
            parameterTypes.firstOrNull { l: List<ID.Type> -> l.size == numberOfIds } ?: throw IllegalArgumentException(
                String.format("Data item %s cannot be used with %d ids", name, numberOfIds))
        return params[index]
    }

    companion object {

        fun fromSymbol(symbol: String): DataItemType {
            return entries.first { op: DataItemType -> op.symbol == symbol }
        }
    }
}
