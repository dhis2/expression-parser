package org.hisp.dhis.lib.expression.js

import org.hisp.dhis.lib.expression.spi.DataItemType

@OptIn(ExperimentalJsExport::class)
@JsExport
data class DataItemJs(
    val type: String,
    val uid0: IDJs,
    val uid1: Array<IDJs>,
    val uid2: Array<IDJs>,
    val modifiers: QueryModifiersJs
) {

    init {
        require(TYPES.contains(type)) { "DataItem type must be one of: $TYPES" }
    }

    companion object {
        val TYPES = DataItemType.entries.map { it.name } .toTypedArray()
    }
}