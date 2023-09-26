package org.hisp.dhis.lib.expression.spi

/**
 * A variable value as used int he rule-engine.
 */
interface VariableValue {
    /**
     * @return variable value, maybe null
     */
    fun value(): String?
    fun valueOrDefault(): Any?

    /**
     * @return list of candidates, never null
     */
    fun candidates(): List<String?>?

    /**
     * @return associated event date, maybe null
     */
    fun eventDate(): String?
    fun valueType(): ValueType
}
