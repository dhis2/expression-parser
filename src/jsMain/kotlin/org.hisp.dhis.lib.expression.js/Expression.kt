package org.hisp.dhis.lib.expression.js

import kotlinx.datetime.LocalDate
import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.ast.AggregationType
import org.hisp.dhis.lib.expression.spi.DataItemType
import org.hisp.dhis.lib.expression.spi.ValueType

@OptIn(ExperimentalJsExport::class)
@JsExport
class Expression(expression: String , mode: String) {

    private val expr: Expression

    init {
        require(MODES.contains(mode)) { "Mode must be one of: $MODES"}
        expr = Expression(expression, Expression.Mode.valueOf(mode))
    }

    fun collectDataItems(): Array<DataItem> {
        return expr.collectDataItems().map(::toDataItemJS).toTypedArray()
    }

    fun collectProgramRuleVariableNames(): Array<String> {
        return expr.collectProgramRuleVariableNames().toTypedArray()
    }

    fun collectProgramVariablesNames(): Array<String> {
        return expr.collectProgramVariablesNames().toTypedArray()
    }

    fun evaluate(unsupported: (String)-> Any?, data: ExpressionData): Any? {
        return expr.evaluate({ name: String -> unsupported(name) }, toExpressionDataJava(data))
    }

    fun collectProgramVariables(): Array<Variable> {
        return expr.collectProgramVariables().map(::toVariableJS).toTypedArray()
    }

    fun collectUIDs(): Array<ID> {
        return expr.collectUIDs().map(::toIdJS).toTypedArray()
    }

    fun describe(displayNames: Array<Entry<String, String>>): String {
        return expr.describe(toMap(displayNames, { it }, { it }))
    }

    fun validate(displayNamesKeys: Array<Entry<String, String>>) {
        expr.validate(toMap(displayNamesKeys, { it }, { e -> ValueType.valueOf(e) }))
    }

    fun collectDataItemForRegenerate(): Array<DataItem> {
        return expr.collectDataItemForRegenerate().map(::toDataItemJS).toTypedArray()
    }

    fun regenerate(dataItemValues: Array<Entry<DataItem, Double>>): String {
        return expr.regenerate(toMap(dataItemValues, ::toDataItemJava) { it })
    }

    fun normalise(): String {
        return expr.normalise()
    }

    override fun toString(): String {
        return expr.toString()
    }

    companion object {
        val MODES = Expression.Mode.entries.map { it.name }.toTypedArray()

        internal fun <Kf, Vf, K, V> toMap(map: Array<Entry<Kf, Vf>>, key: (Kf) -> K, value: (Vf) -> V): Map<K, V> {
            val res : MutableMap<K, V> = mutableMapOf()
            map.forEach { e -> res[key(e.key)] = value(e.value) }
            return res;
        }

        internal fun toDataItemJava(item: DataItem) : org.hisp.dhis.lib.expression.spi.DataItem {
            return org.hisp.dhis.lib.expression.spi.DataItem(
                type = DataItemType.valueOf(item.type),
                uid0 = toIdJava(item.uid0),
                uid1 = item.uid1.map(::toIdJava).toList(),
                uid2 = item.uid2.map(::toIdJava).toList(),
                modifiers = toQueryModifiersJava(item.modifiers))
        }

        internal fun toDataItemJS(item: org.hisp.dhis.lib.expression.spi.DataItem) : DataItem {
            return DataItem(
                type = item.type.name,
                uid0 = toIdJS(item.uid0),
                uid1 = item.uid1.map(::toIdJS).toTypedArray(),
                uid2 = item.uid2.map(::toIdJS).toTypedArray(),
                modifiers = toQueryModifiersJS(item.modifiers))
        }

        internal fun toVariableJS(variable: org.hisp.dhis.lib.expression.spi.Variable) : Variable {
            return Variable(
                name = variable.name.name,
                modifiers = toQueryModifiersJS(variable.modifiers))
        }

        internal fun toIdJS(id: org.hisp.dhis.lib.expression.spi.ID) : ID {
            return ID(type = id.type.name, value = id.value)
        }

        private fun toIdJava(id: ID) : org.hisp.dhis.lib.expression.spi.ID {
            return org.hisp.dhis.lib.expression.spi.ID(type = org.hisp.dhis.lib.expression.spi.ID.Type.valueOf(id.type), id.value)
        }

        private fun toQueryModifiersJS(modifiers: org.hisp.dhis.lib.expression.spi.QueryModifiers) : QueryModifiers {
            return QueryModifiers(
                periodAggregation = modifiers.periodAggregation,
                aggregationType = modifiers.aggregationType?.name,
                maxDate = modifiers.maxDate?.toString(),
                minDate = modifiers.minDate?.toString(),
                periodOffset = modifiers.periodOffset,
                stageOffset = modifiers.stageOffset,
                yearToDate = modifiers.yearToDate,
                subExpression = modifiers.subExpression)
        }

        private fun toQueryModifiersJava(modifiers: QueryModifiers) : org.hisp.dhis.lib.expression.spi.QueryModifiers {
            return org.hisp.dhis.lib.expression.spi.QueryModifiers(
                periodAggregation =  modifiers.periodAggregation,
                aggregationType = modifiers.aggregationType?.map(AggregationType::valueOf),
                maxDate = modifiers.maxDate?.map(LocalDate::parse),
                minDate = modifiers.minDate?.map(LocalDate::parse),
                periodOffset = modifiers.periodOffset,
                stageOffset = modifiers.stageOffset,
                yearToDate = modifiers.yearToDate,
                subExpression = modifiers.subExpression)
        }

        internal fun toExpressionDataJava(data: ExpressionData) : org.hisp.dhis.lib.expression.spi.ExpressionData {
            return org.hisp.dhis.lib.expression.spi.ExpressionData(
                programRuleVariableValues = toMap(data.programRuleVariableValues, {it}, ::toVariableValueJava),
                programVariableValues = toMap(data.programVariableValues, {it}, {it}),
                supplementaryValues = toMap(data.supplementaryValues, {it}, {v -> v.toList()}),
                dataItemValues = toMap(data.dataItemValues, ::toDataItemJava) { it },
                namedValues = toMap(data.namedValues, {it}, {it}))
        }

        private fun toVariableValueJava(value: VariableValue) : org.hisp.dhis.lib.expression.spi.VariableValue {
            return org.hisp.dhis.lib.expression.spi.VariableValue(
                valueType = ValueType.valueOf(value.valueType),
                value = value.value,
                candidates = value.candidates.toList(),
                eventDate = value.eventDate)
        }
    }
}

private fun <T> String.map(transform: (String) -> T): T {
    return transform(this)
}
