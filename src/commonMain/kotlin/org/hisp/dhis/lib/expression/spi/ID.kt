package org.hisp.dhis.lib.expression.spi

/**
 * An identifier of some type as used in [DataItem]s.
 *
 *
 * Mostly these are UIDs but sometimes these are other identifiers.
 *
 * @author Jan Bernitt
 */
data class ID(
    val type: Type,
    val value: String
) {
    enum class Type {
        AttributeUID,
        AttributeOptionComboUID,
        CategoryOptionUID,
        CategoryOptionComboUID,
        CategoryOptionGroupUID,
        DataElementUID,
        DateElementGroupUID,
        DataSetUID,
        ConstantUID,
        IndicatorUID,
        OrganisationUnitGroupUID,
        ProgramUID,
        ProgramIndicatorUID,
        ProgramVariableName,
        ProgramStageUID,

        // not a UID but an Identifier
        ReportingRateType;

        fun isUID(): Boolean {
            return this != ProgramVariableName && this != ReportingRateType;
        }
    }

    override fun toString(): String {
        return value
    }
}
