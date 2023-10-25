package org.hisp.dhis.lib.expression.js

import org.hisp.dhis.lib.expression.spi.ValueType

@OptIn(ExperimentalJsExport::class)
@JsExport
data class VariableValue(
    val valueType: String,
    val value: String?,
    val candidates: Array<String> ,
    val eventDate: String?
) {
    init {
        require(VALUE_TYPES.contains(valueType)) { "VariableValue valueType must be one of $VALUE_TYPES" }
    }

    companion object {
        val VALUE_TYPES = ValueType.entries.map { it.name }.toTypedArray()
    }
}