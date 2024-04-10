package org.hisp.dhis.lib.expression.js

import js.collections.JsMap
import kotlinx.datetime.LocalDate
import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.ExpressionMode
import org.hisp.dhis.lib.expression.spi.ID
import org.hisp.dhis.lib.expression.spi.ValueType

@OptIn(ExperimentalJsExport::class)
@JsExport
class ExpressionJs(expression: String, mode: ExpressionMode) {

    private val expr: Expression = Expression(expression, mode)

    fun collectDataItems(): Array<DataItemJs> {
        return expr.collectDataItems().map(::toDataItemJS).toTypedArray()
    }

    fun collectProgramRuleVariableNames(): Array<String> {
        return expr.collectProgramRuleVariableNames().toTypedArray()
    }

    fun collectProgramVariablesNames(): Array<String> {
        return expr.collectProgramVariablesNames().toTypedArray()
    }

    fun evaluate(unsupported: (String) -> Any?, data: ExpressionDataJs): Any? {
        return expr.evaluate(unsupported, toExpressionDataJava(data))
    }

    fun collectProgramVariables(): Array<VariableJs> {
        return expr.collectProgramVariables().map(::toVariableJS).toTypedArray()
    }

    fun collectUIDs(): Array<ID> {
        return expr.collectUIDs().toTypedArray()
    }

    fun describe(displayNames: JsMap<String, String>): String {
        return expr.describe(toMap(displayNames, { it }, { it }))
    }

    fun validate(displayNamesKeys: JsMap<String, String>) {
        expr.validate(toMap(displayNamesKeys, { it }, { e -> ValueType.valueOf(e) }))
    }

    fun collectDataItemForRegenerate(): Array<DataItemJs> {
        return expr.collectDataItemForRegenerate().map(::toDataItemJS).toTypedArray()
    }

    fun regenerate(dataItemValues: JsMap<DataItemJs, Double>): String {
        return expr.regenerate(toMap(dataItemValues, ::toDataItemJava) { it })
    }

    fun normalise(): String {
        return expr.normalise()
    }

    override fun toString(): String {
        return expr.toString()
    }

    companion object {
        val MODES = ExpressionMode.entries.map { it.name }.toTypedArray()

        internal fun <Kf, Vf, K, V> toMap(map: JsMap<Kf, Vf>, key: (Kf) -> K, value: (Vf) -> V): Map<K, V> {
            val res : MutableMap<K, V> = mutableMapOf()
            map.forEach { v, k -> res[key(k)] = value(v) }
            return res;
        }

        internal fun toDataItemJava(item: DataItemJs) : org.hisp.dhis.lib.expression.spi.DataItem {
            return org.hisp.dhis.lib.expression.spi.DataItem(
                type = item.type,
                uid0 = item.uid0,
                uid1 = item.uid1.toList(),
                uid2 = item.uid2.toList(),
                modifiers = toQueryModifiersJava(item.modifiers))
        }

        internal fun toDataItemJS(item: org.hisp.dhis.lib.expression.spi.DataItem) : DataItemJs {
            return DataItemJs(
                type = item.type,
                uid0 = item.uid0,
                uid1 = item.uid1.toTypedArray(),
                uid2 = item.uid2.toTypedArray(),
                modifiers = toQueryModifiersJS(item.modifiers))
        }

        internal fun toVariableJS(variable: org.hisp.dhis.lib.expression.spi.Variable) : VariableJs {
            return VariableJs(
                name = variable.name,
                modifiers = toQueryModifiersJS(variable.modifiers))
        }

        private fun toQueryModifiersJS(modifiers: org.hisp.dhis.lib.expression.spi.QueryModifiers) : QueryModifiersJs {
            return QueryModifiersJs(
                periodAggregation = modifiers.periodAggregation,
                aggregationType = modifiers.aggregationType,
                maxDate = modifiers.maxDate?.toString(),
                minDate = modifiers.minDate?.toString(),
                periodOffset = modifiers.periodOffset,
                stageOffset = modifiers.stageOffset,
                yearToDate = modifiers.yearToDate,
                subExpression = modifiers.subExpression)
        }

        private fun toQueryModifiersJava(modifiers: QueryModifiersJs) : org.hisp.dhis.lib.expression.spi.QueryModifiers {
            return org.hisp.dhis.lib.expression.spi.QueryModifiers(
                periodAggregation =  modifiers.periodAggregation,
                aggregationType = modifiers.aggregationType,
                maxDate = modifiers.maxDate?.map(LocalDate::parse),
                minDate = modifiers.minDate?.map(LocalDate::parse),
                periodOffset = modifiers.periodOffset,
                stageOffset = modifiers.stageOffset,
                yearToDate = modifiers.yearToDate,
                subExpression = modifiers.subExpression)
        }

        internal fun toExpressionDataJava(data: ExpressionDataJs) : org.hisp.dhis.lib.expression.spi.ExpressionData {
            return org.hisp.dhis.lib.expression.spi.ExpressionData(
                programRuleVariableValues = toMap(data.programRuleVariableValues, {it}, ::toVariableValueJava),
                programVariableValues = toMap(data.programVariableValues, {it}, {it}),
                supplementaryValues = toMap(data.supplementaryValues, {it}, {v -> v.toList()}),
                dataItemValues = toMap(data.dataItemValues, ::toDataItemJava) { it },
                namedValues = toMap(data.namedValues, {it}, {it}))
        }

        private fun toVariableValueJava(value: VariableValueJs) : org.hisp.dhis.lib.expression.spi.VariableValue {
            return org.hisp.dhis.lib.expression.spi.VariableValue(
                valueType = value.valueType,
                value = value.value,
                candidates = value.candidates.toList(),
                eventDate = value.eventDate)
        }
    }
}

private fun <T> String.map(transform: (String) -> T): T {
    return transform(this)
}
