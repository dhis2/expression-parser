package org.hisp.dhis.expression;

public class UID {

    enum Type {
        DataElement,
        DateElementGroup,
        CategoryOption,
        CategoryOptionGroup
    }

    private final Type type;
    private final String id;

    public UID(Type type, String id) {
        this.type = type;
        this.id = id;
    }
}
