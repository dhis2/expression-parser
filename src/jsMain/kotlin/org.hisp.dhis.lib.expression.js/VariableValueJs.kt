package org.hisp.dhis.lib.expression.js

import org.hisp.dhis.lib.expression.spi.ValueType

@JsExport
data class VariableValueJs(
    val valueType: ValueType,
    val value: String?,
    val candidates: Array<String> ,
    val eventDate: String?)