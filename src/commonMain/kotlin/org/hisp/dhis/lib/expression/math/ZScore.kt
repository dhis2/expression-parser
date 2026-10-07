package org.hisp.dhis.lib.expression.math

import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.round

/**
 * @author Zubair Asghar (original in rule engine)
 * @author Jan Bernitt (imported into expression parser)
 */
object ZScore {

    fun value(mode: Mode, parameter: Number?, weight: Number?, gender: String?): Double {
        requireNotNull(gender) { "Gender cannot be null" }
        requireNotNull(parameter) { "Parameter cannot be null" }
        requireNotNull(weight) { "Weight cannot be null" }
        return getZScore(mode, parameter, weight, if (GENDER_MALE_CODES.contains(gender)) 0 else 1)
    }

    private val GENDER_MALE_CODES = setOf("male", "MALE", "Male", "ma", "m", "M", "0", "false")

    /**
     * Weight-for-height switches from the WHO weight-for-length to the weight-for-height standard at this height (cm).
     */
    private const val WFH_HEIGHT_STANDARD_FROM = 87.0

    private fun getZScore(mode: Mode, parameter: Number, measurement: Number, gender: Int): Double {
        val table = if (gender == 1) mode.girl else mode.boy
        val lms = if (mode == Mode.WFH) getInterpolatedRow(table, gender, parameter.toDouble())
                  else getRow(table, gender, parameter.toFloat())
        return getZScore(lms, measurement.toDouble())
    }

    private fun getRow(table: Map<ZScoreTable.Key, ZScoreTable.Lms>, gender: Int, parameter: Float): ZScoreTable.Lms {
        return table[ZScoreTable.Key(gender, parameter)]
            ?: throw IllegalArgumentException("No key exist for provided parameters")
    }

    /**
     * Like WHO Anthro, a length/height between two 0.1 cm rows linearly interpolates the L, M and S of those rows.
     * Interpolation never crosses the switch from the weight-for-length to the weight-for-height standard:
     * just below it the weight-for-length curve is extended from its last two rows instead.
     */
    private fun getInterpolatedRow(table: Map<ZScoreTable.Key, ZScoreTable.Lms>, gender: Int, height: Double): ZScoreTable.Lms {
        val tenths = floor(height * 10 + 1e-9)
        val fraction = height * 10 - tenths
        val lower = getRow(table, gender, (tenths / 10).toFloat())
        if (fraction < 1e-9) {
            return lower
        }
        val upper = if ((tenths + 1) / 10 == WFH_HEIGHT_STANDARD_FROM) {
            val previous = getRow(table, gender, ((tenths - 1) / 10).toFloat())
            ZScoreTable.Lms(2 * lower.l - previous.l, 2 * lower.m - previous.m, 2 * lower.s - previous.s)
        } else getRow(table, gender, ((tenths + 1) / 10).toFloat())
        return ZScoreTable.Lms(
            lower.l + fraction * (upper.l - lower.l),
            lower.m + fraction * (upper.m - lower.m),
            lower.s + fraction * (upper.s - lower.s))
    }

    /**
     * WHO LMS method, with the WHO restricted application beyond +/-3 SD: outside that range the
     * z-score continues in steps of the distance between the 2nd and 3rd SD.
     */
    private fun getZScore(lms: ZScoreTable.Lms, measurement: Double): Double {
        var z = if (lms.l == 0.0) ln(measurement / lms.m) / lms.s
                else ((measurement / lms.m).pow(lms.l) - 1) / (lms.l * lms.s)
        if (z > 3) {
            val sd3 = measurementAt(lms, 3.0)
            z = 3 + (measurement - sd3) / (sd3 - measurementAt(lms, 2.0))
        }
        else if (z < -3) {
            val sd3neg = measurementAt(lms, -3.0)
            z = -3 + (measurement - sd3neg) / (measurementAt(lms, -2.0) - sd3neg)
        }
        val rounded = round(z * 100) / 100
        // avoid returning -0.0 for slightly negative values
        return if (rounded == 0.0) 0.0 else rounded
    }

    private fun measurementAt(lms: ZScoreTable.Lms, z: Double): Double {
        return if (lms.l == 0.0) lms.m * exp(lms.s * z)
               else lms.m * (1 + lms.l * lms.s * z).pow(1 / lms.l)
    }

    enum class Mode(
        val boy: Map<ZScoreTable.Key, ZScoreTable.Lms>,
        val girl: Map<ZScoreTable.Key, ZScoreTable.Lms>
    ) {
        WFA(ZScoreTable.Z_SCORE_WFA_TABLE_BOY, ZScoreTable.Z_SCORE_WFA_TABLE_GIRL),
        HFA(ZScoreTable.Z_SCORE_HFA_TABLE_BOY, ZScoreTable.Z_SCORE_HFA_TABLE_GIRL),
        WFH(ZScoreTable.Z_SCORE_WFH_TABLE_BOY, ZScoreTable.Z_SCORE_WFH_TABLE_GIRL);


    }
}
