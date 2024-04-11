package org.hisp.dhis.lib.expression.ast

import org.hisp.dhis.lib.expression.spi.IDType

/**
 * Tags allowed to use in data item positions before a UID.
 * <pre>
 * tag:UID
</pre> *
 */
enum class Tag(val idType: IDType) {

    deGroup(IDType.DateElementGroupUID),
    coGroup(IDType.CategoryOptionGroupUID),
    co(IDType.CategoryOptionUID),
    PS_EVENTDATE(IDType.ProgramStageUID)
}
