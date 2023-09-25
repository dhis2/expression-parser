package org.hisp.dhis.lib.expression.ast

import org.hisp.dhis.lib.expression.spi.ID

/**
 * Tags allowed to use in data item positions before a UID.
 * <pre>
 * tag:UID
</pre> *
 */
enum class Tag(val idType: ID.Type) {

    deGroup(ID.Type.DateElementGroupUID),
    coGroup(ID.Type.CategoryOptionGroupUID),
    co(ID.Type.CategoryOptionUID),
    PS_EVENTDATE(ID.Type.ProgramStageUID)
}
