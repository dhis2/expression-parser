package org.hisp.dhis.lib.expression.js

@OptIn(ExperimentalJsExport::class)
@JsExport
data class ExpressionData(
    val programRuleVariableValues: Array<Entry<String, VariableValue>>,
    val programVariableValues: Array<Entry<String, Any>>,
    val supplementaryValues: Array<Entry<String, Array<String>>>,
    val dataItemValues: Array<Entry<DataItem, Any>>,
    val namedValues: Array<Entry<String, Any>>
) {
    @JsName("EMPTY")
    constructor() : this(emptyArray(), emptyArray(), emptyArray(), emptyArray(), emptyArray())
}