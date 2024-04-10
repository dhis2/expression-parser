package org.hisp.dhis.lib.expression.spi

enum class IDType {
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