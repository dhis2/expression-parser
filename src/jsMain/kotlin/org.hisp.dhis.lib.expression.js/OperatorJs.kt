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
    val operandsType: ValueType)
