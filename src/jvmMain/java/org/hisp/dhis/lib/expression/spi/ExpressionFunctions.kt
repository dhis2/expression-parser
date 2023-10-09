package org.hisp.dhis.lib.expression.spi

import org.hisp.dhis.lib.expression.ast.BinaryOperator.Companion.modulo
import org.hisp.dhis.lib.expression.math.AggregateMath
import org.hisp.dhis.lib.expression.math.GS1Elements.Companion.fromKey
import org.hisp.dhis.lib.expression.math.ZScore
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.regex.Pattern
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.ln

/**
 * Implementation API for all expression languages functions.
 *
 * @author Jan Bernitt
 */
@Suppress("kotlin:S100")
fun interface ExpressionFunctions {

    fun unsupported(name: String): Any?

    /**
     * Returns first non-null value (of similar typed values).
     *
     * @param values zero or more values
     * @return the first value that is not null, or null if all values are zero or values is of length zero
     */
    fun firstNonNull(values: List<*>): Any? {
        return values.filterNotNull().firstOrNull()
    }

    /**
     * Returns the largest of the values.
     *
     * @param values zero or more values, nulls allowed (ignored)
     * @return maximum value or null if all values are null
     */
    fun greatest(values: List<Number?>): Number? {
        return values.filterNotNull().maxByOrNull { obj: Number -> obj.toDouble() }
    }

    /**
     * Returns conditional value based on the condition. Actually not the equivalent of an if-else block but the ternary
     * operator `condition ? ifValue : elseValue`.
     *
     * @param condition test, maybe null
     * @param ifValue   value when condition is true
     * @param elseValue value when condition if false or null
     * @param <T>       type of value
     * @return either if value or else value based on the condition
    </T> */
    fun <T> ifThenElse(condition: Boolean?, ifValue: T, elseValue: T): T {
        return if (true == condition) ifValue else elseValue
    }

    fun isNotNull(value: Any?): Boolean {
        return value != null;
    }

    fun isNull(value: Any?): Boolean {
        return value == null;
    }

    /**
     * Returns the smallest of the values
     *
     * @param values zero or more values, nulls allowed (ignored)
     * @return minimum value or null if all values are null
     */
    fun least(values: List<Number?>): Number? {
        return values.filterNotNull().minByOrNull { obj: Number -> obj.toDouble() }
    }

    fun log(n: Number?): Double {
        return if (n == null) Double.NaN else ln(n.toDouble())
    }

    fun log10(n: Number?): Double {
        return if (n == null) Double.NaN else kotlin.math.log10(n.toDouble())
    }

    fun removeZeros(n: Number?): Number? {
        return if (n != null && n.toDouble() == 0.0) null else n
    }

    /*
    Aggregate functions...
     */
    fun avg(values: DoubleArray): Double {
        return AggregateMath.avg(values)
    }

    fun count(values: DoubleArray): Double {
        return AggregateMath.count(values)
    }

    fun max(values: DoubleArray): Double {
        return AggregateMath.max(values)
    }

    fun median(values: DoubleArray): Double {
        return AggregateMath.median(values)
    }

    fun min(values: DoubleArray): Double {
        return AggregateMath.min(values)
    }

    fun percentileCont(values: DoubleArray, fraction: Double?): Double? {
        return AggregateMath.percentileCont(values, fraction)
    }

    fun stddev(values: DoubleArray): Double {
        return AggregateMath.stddev(values)
    }

    fun stddevPop(values: DoubleArray): Double {
        return AggregateMath.stddevPop(values)
    }

    fun stddevSamp(values: DoubleArray): Double {
        return AggregateMath.stddevSamp(values)
    }

    fun sum(values: DoubleArray): Double {
        return AggregateMath.sum(values)
    }

    fun variance(values: DoubleArray): Double {
        return AggregateMath.variance(values)
    }

    /*
    D2 functions...
     */
    fun d2_addDays(date: LocalDate?, days: Number?): LocalDate? {
        return if (date == null) null else if (days == null) date else date.plusDays(days.toInt().toLong())
    }

    fun d2_ceil(value: Number?): Double {
        return if (value == null) 0.0 else ceil(value.toDouble())
    }

    fun d2_concatenate(values: Collection<String?>): String? {
        return values.filterNotNull().joinToString("")
    }

    /**
     * Counts the number of values that is entered for the source field in the argument. The source field parameter is
     * the name of one of the defined source fields in the program.
     *
     * @return the number of [VariableValue.candidates]
     */
    fun d2_count(value: VariableValue?): Int {
        return value?.candidates()?.size ?: 0
    }

    fun d2_countIfValue(value: VariableValue?, booleanOrNumber: Any?): Int {
        return if (value == null || booleanOrNumber == null) 0
        else Collections.frequency(
            value.candidates(),
            booleanOrNumber)
    }

    fun d2_countIfZeroPos(value: VariableValue?): Int {
        return value?.candidates()?.count { n: String -> n.toDouble() >= 0.0 } ?: 0
    }

    fun d2_daysBetween(start: LocalDate?, end: LocalDate?): Int {
        return ChronoUnit.DAYS.between(start, end).toInt()
    }

    fun d2_extractDataMatrixValue(gs1Key: String?, value: String?): String? {
        return fromKey(gs1Key!!).format(value)
    }

    fun d2_floor(value: Number?): Double {
        return if (value == null) 0.0 else floor(value.toDouble())
    }

    fun d2_hasUserRole(role: String?, roles: List<String?>?): Boolean {
        if (roles == null) throw IllegalExpressionException("Supplementary data for user needs to be provided")
        return roles.contains(role)
    }

    fun d2_hasValue(value: VariableValue?): Boolean {
        return value?.value() != null
    }

    fun d2_inOrgUnitGroup(
        group: String?,
        orgUnit: VariableValue?,
        supplementaryValues: Map<String, List<String>>
    ): Boolean {
        val members = supplementaryValues[group]
        val uid = if (orgUnit == null) "" else orgUnit.value()?.replace("'", "")
        return members != null && members.contains(uid)
    }

    fun d2_lastEventDate(value: VariableValue?): LocalDate? {
        return if (value == null) null else LocalDate.parse(value.eventDate())
    }

    fun d2_left(input: String?, length: Int?): String? {
        return if (input == null || length == null) "" else input.substring(0, Math.min(input.length, length))
    }

    fun d2_length(str: String?): Int {
        return str?.length ?: 0
    }

    fun d2_maxValue(value: VariableValue?): Double {
        return if (value == null) Double.NaN
        else value.candidates().stream().mapToDouble { s: String -> s.toDouble() }.max().orElse(
            Double.NaN)
    }

    fun d2_minutesBetween(start: LocalDate?, end: LocalDate?): Int {
        return ChronoUnit.MINUTES.between(start, end).toInt()
    }

    fun d2_minValue(value: VariableValue?): Double {
        return if (value == null) Double.NaN
        else value.candidates().stream().mapToDouble { s: String -> s.toDouble() }.min().orElse(
            Double.NaN)
    }

    fun d2_modulus(left: Number?, right: Number?): Double {
        return modulo(left, right).toDouble()
    }

    fun d2_monthsBetween(start: LocalDate?, end: LocalDate?): Int {
        return ChronoUnit.MONTHS.between(start, end).toInt()
    }

    fun d2_oizp(value: Number?): Double {
        return if (value != null && value.toDouble() >= 0.0) 1.0 else 0.0
    }

    fun d2_right(input: String?, length: Int?): String? {
        return if (input == null || length == null) "" else input.substring(input.length - length)
    }

    fun d2_round(value: Number?, precision: Int?): Double {
        var precision = precision
        if (value == null) return Double.NaN
        precision = precision ?: 0
        val roundedNumber = BigDecimal.valueOf(value.toDouble()).setScale(precision, RoundingMode.HALF_UP)
        return if (precision == 0 || roundedNumber.toInt().toDouble() == roundedNumber.toDouble()) roundedNumber.toInt()
            .toDouble()
        else roundedNumber.stripTrailingZeros().toDouble()
    }

    /**
     * Split the text by delimiter, and keep the nth element(0 is the first).
     */
    fun d2_split(input: String?, delimiter: String?, index: Int?): String? {
        if (input == null || delimiter == null) return ""
        val parts = input.split(Pattern.quote(delimiter).toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return if (index == null || index < 0 || index >= parts.size) "" else parts[index]
    }

    fun d2_substring(input: String?, beginIndex: Int?, endIndex: Int?): String? {
        return input?.substring(beginIndex!!, endIndex!!) ?: ""
    }

    fun d2_validatePattern(input: String?, regex: String?): Boolean {
        return input != null && regex != null && input.matches(regex.toRegex())
    }

    fun d2_weeksBetween(start: LocalDate?, end: LocalDate?): Int {
        return ChronoUnit.WEEKS.between(start, end).toInt()
    }

    fun d2_yearsBetween(start: LocalDate?, end: LocalDate?): Int {
        return ChronoUnit.YEARS.between(start, end).toInt()
    }

    /**
     * Evaluates the argument of type number to zero if the value is negative, otherwise to the value itself.
     */
    fun d2_zing(value: Number?): Number? {
        return if (value != null && value.toDouble() < 0.0) if (value is Double) 0.0 else 0 else value
    }

    /**
     * Returns the number of numeric zero and positive values among the given object arguments. Can be provided with any
     * number of arguments.
     */
    fun d2_zpvc(values: List<Number?>): Double {
        return values.count { n -> n != null && n.toDouble() >= 0.0 }
            .toDouble()
    }

    fun d2_zScoreHFA(parameter: Number?, weight: Number?, gender: String?): Double {
        return ZScore.value(ZScore.Mode.HFA, parameter, weight, gender)
    }

    fun d2_zScoreWFA(parameter: Number?, weight: Number?, gender: String?): Double {
        return ZScore.value(ZScore.Mode.WFA, parameter, weight, gender)
    }

    fun d2_zScoreWFH(parameter: Number?, weight: Number?, gender: String?): Double {
        return ZScore.value(ZScore.Mode.WFH, parameter, weight, gender)
    }
}
