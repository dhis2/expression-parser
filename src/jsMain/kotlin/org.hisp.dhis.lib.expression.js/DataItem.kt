package org.hisp.dhis.lib.expression.js

import org.hisp.dhis.lib.expression.spi.DataItemType

@OptIn(ExperimentalJsExport::class)
@JsExport
data class DataItem(
    val type: String,
    val uid0: ID,
    val uid1: Array<ID>,
    val uid2: Array<ID>,
    val modifiers: QueryModifiers
) {

    init {
        require(TYPES.contains(type)) { "DataItem type must be one of: $TYPES" }
    }

    companion object {
        val TYPES = DataItemType.entries.map { it.name } .toTypedArray()
    }
}