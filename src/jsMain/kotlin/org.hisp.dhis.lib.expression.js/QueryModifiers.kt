package org.hisp.dhis.lib.expression.js

import kotlinx.datetime.LocalDate
import org.hisp.dhis.lib.expression.ast.AggregationType

@OptIn(ExperimentalJsExport::class)
@JsExport
data class QueryModifiers(
    val periodAggregation: Boolean,
    val aggregationType: String?,
    val maxDate: String?,
    val minDate: String?,
    val periodOffset: Int?,
    val stageOffset: Int?,
    val yearToDate: Boolean,
    val subExpression: String? // SQL
) {
    init {
        require(AGGREGATION_TYPES.contains(aggregationType)) { "QueryModifiers aggregationType must be one of: $AGGREGATION_TYPES" }
        requireDoesNotThrow(maxDate, LocalDate::parse)
        requireDoesNotThrow(minDate, LocalDate::parse)
    }

    companion object {
        val AGGREGATION_TYPES = AggregationType.entries.map { it.name }.toTypedArray()

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