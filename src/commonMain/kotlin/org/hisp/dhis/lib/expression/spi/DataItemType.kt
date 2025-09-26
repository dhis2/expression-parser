package org.hisp.dhis.lib.expression.spi

import kotlin.js.JsExport

/**
 * The different types of data value references in the expression grammar.
 *
 * @author Jan Bernitt
 */
@JsExport
enum class DataItemType(internal val symbol: String, private val parameterTypes: List<List<IDType>>) {

    // (data element for aggregate vs. program stage . data element for programs)
    DATA_ELEMENT(
        "#", listOf(
            listOf(IDType.DataElementUID),
            listOf(IDType.ProgramStageUID, IDType.DataElementUID),
            listOf(IDType.DataElementUID, IDType.CategoryOptionUID, IDType.AttributeOptionComboUID)
        )
    ),

    // (Program Attribute, not currently used for aggregate data)
    ATTRIBUTE(
        "A", listOf(
            listOf(IDType.AttributeUID),
            listOf(IDType.ProgramUID, IDType.AttributeUID)
        )
    ),
    CONSTANT("C", IDType.ConstantUID),

    // (Not to be confused with #, also used in programs)
    PROGRAM_DATA_ELEMENT("D", IDType.ProgramUID, IDType.DataElementUID),
    PROGRAM_INDICATOR("I", IDType.ProgramIndicatorUID),

    // ('I' was already taken for program indicator)
    INDICATOR("N", IDType.IndicatorUID),
    ORG_UNIT_GROUP("OUG", IDType.OrganisationUnitGroupUID),
    REPORTING_RATE("R", IDType.DataSetUID, IDType.ReportingRateType),
    PROGRAM_VARIABLE("V", IDType.ProgramVariableName),
    ANDROID_CUSTOM_INTENT("VAR", IDType.AndroidCustomIntent);

    constructor(symbol: String, vararg parameterTypes: IDType) : this(
        symbol, listOf<List<IDType>>(listOf<IDType>(*parameterTypes)))

    fun getType(numberOfIds: Int, index: Int): IDType {
        val params =
            parameterTypes.firstOrNull { l: List<IDType> -> l.size == numberOfIds } ?: throw IllegalArgumentException(
                "Data item $name cannot be used with $numberOfIds ids")
        return params[index]
    }

    companion object {

        fun fromSymbol(symbol: String): DataItemType {
            return entries.first { op: DataItemType -> op.symbol == symbol }
        }
    }
}
