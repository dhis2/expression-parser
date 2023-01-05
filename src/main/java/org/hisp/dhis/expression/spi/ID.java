package org.hisp.dhis.expression.spi;

import lombok.Value;

@Value
public class ID {

    @SuppressWarnings("java:S115")
    public enum Type {
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
        ReportingRateType
    }

    Type type;
    String value;

    @Override
    public String toString() {
        return value;
    }
}
