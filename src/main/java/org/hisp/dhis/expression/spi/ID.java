package org.hisp.dhis.expression.spi;

import lombok.Value;

/**
 * An identifier of some type as used in {@link DataItem}s.
 *
 * Mostly these are UIDs but sometimes these are other identifiers.
 *
 * @author Jan Bernitt
 */
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
        ReportingRateType;

        public boolean isUID() {
            return this != ProgramVariableName && this != ReportingRateType;
        }
    }

    Type type;
    String value;

    @Override
    public String toString() {
        return value;
    }
}
