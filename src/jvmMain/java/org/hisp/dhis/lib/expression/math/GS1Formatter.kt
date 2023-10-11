package org.hisp.dhis.lib.expression.math

import org.hisp.dhis.lib.expression.math.GS1Elements.Companion.fromKey
import org.hisp.dhis.lib.expression.math.GS1Elements.Companion.getApplicationIdentifier

object GS1Formatter {

    fun format(value: String?, valueToReturn: GS1Elements): String {
        requireNotNull(value) { "Can't extract data from null value" }
        require(value.length >= 3) { "Value does not contains enough information" }
        val gs1Identifier = value.substring(0, 3)
        return when (fromKey(gs1Identifier)) {
            GS1Elements.GS1_d2_IDENTIFIER, GS1Elements.GS1_Q3_IDENTIFIER -> formatValue(
                value,
                valueToReturn)

            GS1Elements.GS1_J1_IDENTIFIER, GS1Elements.GS1_d1_IDENTIFIER, GS1Elements.GS1_Q1_IDENTIFIER, GS1Elements.GS1_E0_IDENTIFIER, GS1Elements.GS1_E1_IDENTIFIER, GS1Elements.GS1_E2_IDENTIFIER, GS1Elements.GS1_E3_IDENTIFIER, GS1Elements.GS1_E4_IDENTIFIER, GS1Elements.GS1_I1_IDENTIFIER, GS1Elements.GS1_C1_IDENTIFIER, GS1Elements.GS1_e0_IDENTIFIER, GS1Elements.GS1_e1_IDENTIFIER, GS1Elements.GS1_e2_IDENTIFIER -> throw IllegalArgumentException(
                "gs1 identifier $gs1Identifier is not supported")

            else -> throw IllegalArgumentException("Value does not start with a gs1 identifier")
        }
    }

    private fun removeGS1Identifier(value: String): String {
        return value.substring(3)
    }

    private fun formatValue(value: String, valueToReturn: GS1Elements): String {
        val dataMap: MutableMap<String, String> = HashMap()
        val gs1Groups = removeGS1Identifier(value).split(GS1Elements.GS1_GROUP_SEPARATOR.element.toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
        for (gs1Group in gs1Groups) {
            handleGroupData(gs1Group, dataMap)
        }
        return dataMap.getOrElse(
            valueToReturn.element
        ) { throw IllegalArgumentException("Required key does not exist for provided value") }
    }

    private fun handleGroupData(gs1Group: String, dataMap: MutableMap<String, String>) {
        if (gs1Group.isNotEmpty()) {
            val gs1GroupLength = gs1Group.length
            val ai = getApplicationIdentifier(gs1Group)
            var nextValueLength = AI_FIXED_LENGTH[ai.substring(0, 2)]
            if (nextValueLength == null) nextValueLength = gs1GroupLength
            dataMap[ai] = gs1Group.substring(ai.length, nextValueLength)
            handleGroupData(gs1Group.substring(nextValueLength), dataMap)
        }
    }

    private val AI_FIXED_LENGTH: MutableMap<String, Int> = HashMap()

    init {
        AI_FIXED_LENGTH[GS1Elements.SSCC.element] = 20
        AI_FIXED_LENGTH[GS1Elements.GTIN.element] = 16
        AI_FIXED_LENGTH[GS1Elements.CONTENT.element] = 16
        AI_FIXED_LENGTH["03"] = 16
        AI_FIXED_LENGTH["04"] = 18
        AI_FIXED_LENGTH[GS1Elements.PROD_DATE.element] = 8
        AI_FIXED_LENGTH[GS1Elements.DUE_DATE.element] = 8
        AI_FIXED_LENGTH[GS1Elements.PACK_DATE.element] = 8
        AI_FIXED_LENGTH["14"] = 8
        AI_FIXED_LENGTH[GS1Elements.BEST_BEFORE_DATE.element] = 8
        AI_FIXED_LENGTH[GS1Elements.SELL_BY.element] = 8
        AI_FIXED_LENGTH[GS1Elements.EXP_DATE.element] = 8
        AI_FIXED_LENGTH["18"] = 8
        AI_FIXED_LENGTH["19"] = 8
        AI_FIXED_LENGTH[GS1Elements.VARIANT.element] = 4
        AI_FIXED_LENGTH["31"] = 10
        AI_FIXED_LENGTH["32"] = 10
        AI_FIXED_LENGTH["33"] = 10
        AI_FIXED_LENGTH["34"] = 10
        AI_FIXED_LENGTH["35"] = 10
        AI_FIXED_LENGTH["36"] = 10
        AI_FIXED_LENGTH["41"] = 16
    }
}