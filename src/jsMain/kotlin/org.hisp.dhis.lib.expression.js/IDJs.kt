package org.hisp.dhis.lib.expression.js

import org.hisp.dhis.lib.expression.spi.ID

@OptIn(ExperimentalJsExport::class)
@JsExport
data class IDJs(
    val type: String,
    val value: String
) {
    init {
        require(TYPES.contains(type)) { "ID type must be one of: $TYPES" }
    }

    companion object {
        val TYPES = ID.Type.entries.map { it.name }.toTypedArray()
    }
}