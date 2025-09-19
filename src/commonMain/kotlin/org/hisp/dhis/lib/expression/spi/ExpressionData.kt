package org.hisp.dhis.lib.expression.spi

/**
 * The value context used during expression evaluation.
 *
 *
 * These are the values plugged into the expression for data items, variables or the general context.
 */
data class ExpressionData(
    val programRuleVariableValues: Map<String, VariableValue>,
    val programVariableValues: Map<String, Any>,
    val supplementaryValues: Map<SupplementaryKey, List<String>>,
    val dataItemValues: Map<DataItem, Any>,
    val namedValues: Map<String, Any>
) {
    constructor() : this(mapOf(), mapOf(), mapOf(), mapOf(), mapOf())
}
