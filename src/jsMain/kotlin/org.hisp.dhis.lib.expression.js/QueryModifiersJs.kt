package org.hisp.dhis.lib.expression.js

import kotlinx.datetime.LocalDate
import org.hisp.dhis.lib.expression.spi.AggregationType

@OptIn(ExperimentalJsExport::class)
@JsExport
data class QueryModifiersJs(
    val periodAggregation: Boolean,
    val aggregationType: AggregationType?,
    val maxDate: String?,
    val minDate: String?,
    val periodOffset: Int?,
    val stageOffset: Int?,
    val yearToDate: Boolean,
    val subExpression: String? // SQL
) {
    init {
        requireDoesNotThrow(maxDate, LocalDate::parse)
        requireDoesNotThrow(minDate, LocalDate::parse)
    }

    companion object {
        internal fun <T> requireDoesNotThrow(value: T?, f: (T) -> Any?) {
            if (value == null) return
            try {
                f(value)
            } catch (ex: Exception) {
                require(false) { "Not a valid value: $value"}
            }
        }
    }
}