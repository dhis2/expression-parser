package org.hisp.dhis.expression.ast;

public class UID {

    enum Type {
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
        ProgramStage
    }

    private final Type type;
    private final String value;

    public UID(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
