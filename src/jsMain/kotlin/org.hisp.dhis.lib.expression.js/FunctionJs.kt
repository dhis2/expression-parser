package org.hisp.dhis.lib.expression.js

import org.hisp.dhis.lib.expression.spi.ValueType

@JsExport
data class FunctionJs(
    val name: String,
    val returnType: ValueType,
    val parameters: Array<ParameterJs>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.js != other::class.js) return false

        other as FunctionJs

        if (name != other.name) return false
        if (returnType != other.returnType) return false
        if (!parameters.contentEquals(other.parameters)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + returnType.hashCode()
        result = 31 * result + parameters.contentHashCode()
        return result
    }
}

@JsExport
data class ParameterJs(
    val type: ValueType,
    val isOptional: Boolean,
    val isVarargs: Boolean,
    val name: String?)
