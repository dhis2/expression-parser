package org.hisp.dhis.lib.expression.js

import js.collections.JsMap

@OptIn(ExperimentalJsExport::class)
@JsExport
data class ExpressionDataJs(
    val programRuleVariableValues: JsMap<String, VariableValueJs> = JsMap(),
    val programVariableValues: JsMap<String, Any> = JsMap(),
    val supplementaryValues: JsMap<String, Array<String>> = JsMap(),
    val dataItemValues: JsMap<DataItemJs, Any> = JsMap(),
    val namedValues: JsMap<String, Any> = JsMap()
)