package org.hisp.dhis.lib.expression.js

import org.hisp.dhis.lib.expression.spi.ValueType

@OptIn(ExperimentalJsExport::class)
@JsExport
data class VariableValueJs(
    val valueType: ValueType,
    val value: String?,
    val candidates: Array<String> ,
    val eventDate: String?)