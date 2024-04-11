package org.hisp.dhis.lib.expression.js

import org.hisp.dhis.lib.expression.spi.DataItemType
import org.hisp.dhis.lib.expression.spi.ID

@JsExport
data class DataItemJs(
    val type: DataItemType,
    val uid0: ID,
    val uid1: Array<ID>,
    val uid2: Array<ID>,
    val modifiers: QueryModifiersJs
)