package org.hisp.dhis.expression.spi;

import lombok.Value;

@Value
public class UID {

    @SuppressWarnings("java:S115")
    public enum Type {
        Attribute,
        AttributeOptionCombo,
        CategoryOption,
        CategoryOptionCombo,
        CategoryOptionGroup,
        DataElement,
        DateElementGroup,
        DataSet,
        Constant,
        Indicator,
        OrganisationUnitGroup,
        Program,
        ProgramIndicator,
        ProgramVariable,
        ProgramStage,
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
