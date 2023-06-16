package org.hisp.dhis.lib.expression.ast;

import org.hisp.dhis.lib.expression.spi.ID;

/**
 * Tags allowed to use in data item positions before a UID.
 * <pre>
 *     tag:UID
 * </pre>
 */
@SuppressWarnings("java:S115")
public enum Tag {
    deGroup(ID.Type.DateElementGroupUID),
    coGroup(ID.Type.CategoryOptionGroupUID),
    co(ID.Type.CategoryOptionUID),
    PS_EVENTDATE(ID.Type.ProgramStageUID);

    private final ID.Type type;

    Tag(ID.Type type) {
        this.type = type;
    }

    public ID.Type getIdType() {
        return type;
    }
}
