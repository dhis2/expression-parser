package org.hisp.dhis.lib.expression.js

import org.hisp.dhis.lib.expression.spi.ValueType

@JsExport
data class FunctionJs(
    val name: String,
    val returnType: ValueType,
    val parameters: Array<ParameterJs>)

@JsExport
data class ParameterJs(
    val type: ValueType,
    val isOptional: Boolean,
    val isVarargs: Boolean)
