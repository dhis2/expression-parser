package org.hisp.dhis.lib.expression.spi

import org.hisp.dhis.lib.expression.ast.AggregationType
import java.time.LocalDate

data class QueryModifiers (
    /**
     * Use aggregation over periods when loading data. Value then must be a `double[]`.
     */
    val periodAggregation: Boolean,
    val aggregationType: AggregationType?,
    val maxDate: LocalDate?,
    val minDate: LocalDate?,
    val periodOffset: Int?,
    val stageOffset: Int?,
    val yearToDate: Boolean,
    val subExpression: String? // SQL
){

    constructor() : this(false, null, null, null, null, null, false, null)

    override fun toString(): String {
        val str = StringBuilder()
        val toStr = { name: String?, value: Any? ->
            if (value != null) {
                if (value is Boolean) {
                    if (value == true) str.append(".").append(name).append("()");
                } else {
                    str.append(".").append(name).append("(").append(value.toString()).append(")")
                }
            }
        }
        toStr("periodAggregation", periodAggregation)
        toStr("aggregationType", aggregationType)
        toStr("maxDate", maxDate)
        toStr("minDate", minDate)
        toStr("periodOffset", periodOffset)
        toStr("stageOffset", stageOffset)
        toStr("yearToDate", yearToDate)
        return str.toString()
    }
}
