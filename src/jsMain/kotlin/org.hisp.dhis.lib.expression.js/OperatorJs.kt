package org.hisp.dhis.lib.expression.js

import org.hisp.dhis.lib.expression.spi.ValueType

/**
 * @param operandsType the type expected for the operand(s)
 */
@JsExport
data class OperatorJs(
    val symbol: String,
    val aliases: Array<String>,
    val returnType: ValueType,
    val operandsType: ValueType) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.js != other::class.js) return false

        other as OperatorJs

        if (symbol != other.symbol) return false
        if (!aliases.contentEquals(other.aliases)) return false
        if (returnType != other.returnType) return false
        if (operandsType != other.operandsType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = symbol.hashCode()
        result = 31 * result + aliases.contentHashCode()
        result = 31 * result + returnType.hashCode()
        result = 31 * result + operandsType.hashCode()
        return result
    }
}
