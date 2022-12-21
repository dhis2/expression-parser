package org.hisp.dhis.expression.ast;

/**
 * Tags allowed to use in data item positions before a UID.
 * <pre>
 *     tag:UID
 * </pre>
 */
@SuppressWarnings("java:S115")
public enum Tag
{
    deGroup(UID.Type.DateElementGroup),
    coGroup(UID.Type.CategoryOptionGroup),
    co(UID.Type.CategoryOption),
    PS_EVENTDATE(null);

    private final UID.Type type;

    Tag(UID.Type type) {
        this.type = type;
    }

    public UID.Type getIdType() {
        return type;
    }
}
