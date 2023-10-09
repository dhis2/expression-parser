package org.hisp.dhis.lib.expression.math

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

/**
 * @author Zubair Asghar (original in rule engine)
 * @author Jan Bernitt (imported into expression parser)
 */
object ZScore {

    fun value(mode: Mode, parameter: Number?, weight: Number?, gender: String?): Double {
        requireNotNull(gender) { "Gender cannot be null" }
        requireNotNull(parameter) { "Parameter cannot be null" }
        requireNotNull(weight) { "Weight cannot be null" }
        return getZScore(mode, parameter.toFloat(), weight.toFloat(), if (GENDER_CODES.contains(gender)) 0 else 1)
    }

    private val GENDER_CODES = setOf("male", "MALE", "Male", "ma", "m", "M", "0", "false")
    private fun getZScore(mode: Mode, parameter: Float, weight: Float, gender: Int): Double {
        val key = ZScoreTable.Key(gender, parameter)
        val table = (if (gender == 1) mode.girl[key] else mode.boy[key])
            ?: throw IllegalArgumentException("No key exist for provided parameters")
        val multiplicationFactor = getMultiplicationFactor(table, weight)

        // weight exactly matches with any of the SD values
        if (table.sdMap.containsKey(weight)) {
            val sd = table.sdMap[weight]!!
            return sd.toDouble() * multiplicationFactor
        }

        // weight is beyond -3SD or 3SD
        if (weight > table.max) {
            return 3.5
        }
        else if (weight < table.min) {
            return -3.5
        }
        var lowerLimitX = 0f
        var higherLimitY = 0f

        // find the interval
        for (f in table.sortedKeys) {
            if (weight > f) {
                lowerLimitX = f
                continue
            }
            higherLimitY = f
            break
        }
        val distance = higherLimitY - lowerLimitX
        val gap: Float
        val decimalAddition: Float
        var result: Float
        if (weight > findMedian(table)) {
            gap = weight - lowerLimitX
            decimalAddition = gap / distance
            result = table.sdMap[lowerLimitX]!! + decimalAddition
        }
        else {
            gap = higherLimitY - weight
            decimalAddition = gap / distance
            result = table.sdMap[higherLimitY]!! + decimalAddition
        }
        result *= multiplicationFactor
        return decimalFormat.format(result.toDouble()).toDouble()
    }

    private val decimalFormat: DecimalFormat
        get() {
            val symbols = DecimalFormatSymbols.getInstance()
            symbols.decimalSeparator = '.'
            return DecimalFormat("##0.00", symbols)
        }

    private fun getMultiplicationFactor(table: ZScoreTable.Entry, weight: Float): Int {
        return weight.compareTo(findMedian(table))
    }

    private fun findMedian(table: ZScoreTable.Entry): Float {
        return table.sortedKeys[3]
    }

    enum class Mode(
        val boy: Map<ZScoreTable.Key, ZScoreTable.Entry>,
        val girl: Map<ZScoreTable.Key, ZScoreTable.Entry>
    ) {
        WFA(ZScoreTable.Z_SCORE_WFA_TABLE_BOY, ZScoreTable.Z_SCORE_WFA_TABLE_GIRL),
        HFA(ZScoreTable.Z_SCORE_HFA_TABLE_BOY, ZScoreTable.Z_SCORE_HFA_TABLE_GIRL),
        WFH(ZScoreTable.Z_SCORE_WFH_TABLE_BOY, ZScoreTable.Z_SCORE_WFH_TABLE_GIRL);


    }

}
