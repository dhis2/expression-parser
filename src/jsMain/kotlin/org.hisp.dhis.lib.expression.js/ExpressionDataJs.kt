package org.hisp.dhis.lib.expression.js

@OptIn(ExperimentalJsExport::class)
@JsExport
data class ExpressionDataJs(
    val programRuleVariableValues: Array<Entry<String, VariableValueJs>>,
    val programVariableValues: Array<Entry<String, Any>>,
    val supplementaryValues: Array<Entry<String, Array<String>>>,
    val dataItemValues: Array<Entry<DataItemJs, Any>>,
    val namedValues: Array<Entry<String, Any>>
) {
    @JsName("EMPTY")
    constructor() : this(emptyArray(), emptyArray(), emptyArray(), emptyArray(), emptyArray())
}