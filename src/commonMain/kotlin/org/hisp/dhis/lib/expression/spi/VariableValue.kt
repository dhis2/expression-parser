package org.hisp.dhis.lib.expression.spi

/**
 * A variable value as used int he rule-engine.
 */
data class VariableValue(
    /**
     * variable value, maybe null
     */
    val value: String?,

    /**
     * list of candidates, never null
     */
    val candidates: List<String> ,

    /**
     * associated event date, maybe null
     */
    val eventDate: String?,
    val valueType: ValueType
) {
    constructor(valueType: ValueType) : this(null, listOf(), null, valueType)

    init {
        require(valueType != ValueType.SAME && valueType != ValueType.MIXED)
    }

    fun valueOrDefault() : Any {
        return value?: valueType.default
    }
}
