package org.hisp.dhis.lib.expression.js

@OptIn(ExperimentalJsExport::class)
@JsExport
data class ExpressionDataJs(
    val programRuleVariableValues: Array<Entry<String, VariableValueJs>> = emptyArray(),
    val programVariableValues: Array<Entry<String, Any>> = emptyArray(),
    val supplementaryValues: Array<Entry<String, Array<String>>> = emptyArray(),
    val dataItemValues: Array<Entry<DataItemJs, Any>> = emptyArray(),
    val namedValues: Array<Entry<String, Any>> = emptyArray()
)