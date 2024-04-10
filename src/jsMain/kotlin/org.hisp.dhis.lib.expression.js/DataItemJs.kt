package org.hisp.dhis.lib.expression.js

import org.hisp.dhis.lib.expression.spi.DataItemType
import org.hisp.dhis.lib.expression.spi.ID

@OptIn(ExperimentalJsExport::class)
@JsExport
data class DataItemJs(
    val type: String,
    val uid0: ID,
    val uid1: Array<ID>,
    val uid2: Array<ID>,
    val modifiers: QueryModifiersJs
) {

    init {
        require(TYPES.contains(type)) { "DataItem type must be one of: $TYPES" }
    }

    companion object {
        val TYPES = DataItemType.entries.map { it.name } .toTypedArray()
    }
}