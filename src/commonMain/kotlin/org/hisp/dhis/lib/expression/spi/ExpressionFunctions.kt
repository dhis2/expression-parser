package org.hisp.dhis.lib.expression.spi

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.*
import org.hisp.dhis.lib.expression.ast.BinaryOperator.Companion.modulo
import org.hisp.dhis.lib.expression.math.VectorAggregation
import org.hisp.dhis.lib.expression.math.GS1Elements.Companion.fromKey
import org.hisp.dhis.lib.expression.math.ZScore
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
        return VectorAggregation.avg(values)
    }

    fun count(values: DoubleArray): Double {
        return VectorAggregation.count(values)
    }

    fun max(values: DoubleArray): Double {
        return VectorAggregation.max(values)
    }

    fun median(values: DoubleArray): Double? {
        return VectorAggregation.median(values)
    }

    fun min(values: DoubleArray): Double {
        return VectorAggregation.min(values)
    }

    fun percentileCont(values: DoubleArray, fraction: Double?): Double? {
        return VectorAggregation.percentileCont(values, fraction)
    }

    fun stddev(values: DoubleArray): Double {
        return VectorAggregation.stddev(values)
    }

    fun stddevPop(values: DoubleArray): Double {
        return VectorAggregation.stddevPop(values)
    }

    fun stddevSamp(values: DoubleArray): Double {
        return VectorAggregation.stddevSamp(values)
    }

    fun sum(values: DoubleArray): Double {
        return VectorAggregation.sum(values)
    }

    fun variance(values: DoubleArray): Double? {
        if (values.isEmpty()) return null
        return VectorAggregation.variance(values)
    }

    /*
    D2 functions...
     */
    fun d2_addDays(date: LocalDate?, days: Number?): LocalDate? {
        return if (date == null) null else if (days == null) date else date.plus(DatePeriod(days=days.toInt()))
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

    fun d2_countIfValue(value: VariableValue?, sample: String?): Int {
        return if (value == null || sample == null) 0
        else value.candidates().count { e -> e == sample }
    }

    fun d2_countIfZeroPos(value: VariableValue?): Int {
        return value?.candidates()?.count { n: String -> n.toDouble() >= 0.0 } ?: 0
    }

    fun d2_daysBetween(start: LocalDate?, end: LocalDate?): Int {
        require(start != null) { "start parameter of d2:daysBetween must not be null" }
        require(end != null) { "end parameter of d2:daysBetween must not be null" }
        return if (start.toEpochDays() < end.toEpochDays()) start.daysUntil(end) else end.daysUntil(start)
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
        return if (value?.eventDate() == null) null else LocalDate.parse(value.eventDate()!!)
    }

    fun d2_left(input: String?, length: Int?): String? {
        return if (input == null || length == null) "" else input.substring(0, input.length.coerceAtMost(length))
    }

    fun d2_length(str: String?): Int {
        return str?.length ?: 0
    }

    fun d2_maxValue(value: VariableValue?): Double {
        return if (value == null) Double.NaN
        else value.candidates().maxOfOrNull { str -> str.toDouble() } ?: Double.NaN
    }

    fun d2_minutesBetween(start: LocalDate?, end: LocalDate?): Int {
        require(start != null) { "start parameter of d2:minutesBetween must not be null" }
        require(end != null) { "end parameter of d2:minutesBetween must not be null" }
        return d2_daysBetween(start, end).times(24 * 60)
    }

    fun d2_minValue(value: VariableValue?): Double {
        return if (value == null) Double.NaN
        else value.candidates().minOfOrNull { str -> str.toDouble() } ?: Double.NaN
    }

    fun d2_modulus(left: Number?, right: Number?): Double {
        return modulo(left, right).toDouble()
    }

    fun d2_monthsBetween(start: LocalDate?, end: LocalDate?): Int {
        require(start != null) { "start parameter of d2:monthsBetween must not be null" }
        require(end != null) { "end parameter of d2:monthsBetween must not be null" }
        return if (start.toEpochDays() < end.toEpochDays()) start.monthsUntil(end) else end.monthsUntil(start)
    }

    fun d2_oizp(value: Number?): Double {
        return if (value != null && value.toDouble() >= 0.0) 1.0 else 0.0
    }

    fun d2_right(input: String?, length: Int?): String? {
        return if (input == null || length == null) "" else input.substring((input.length - length).coerceAtLeast(0))
    }

    fun d2_round(value: Number?, precision: Int?): Double {
        var precision = precision
        if (value == null || value.toDouble().isNaN() || value.toDouble().isInfinite()) return Double.NaN
        precision = precision ?: 0

        val roundedNumber = BigDecimal.fromDouble(value.toDouble()).scale(precision.toLong())
        if (precision == 0 || roundedNumber.isWholeNumber())
            return roundedNumber.toStringExpanded().toDouble()
        val str = roundedNumber.toStringExpanded()
        if (!str.contains('.')) return str.toDouble()
        var len = str.length
        while (str[len-1] == '0') len--;
        return str.substring(0, len).toDouble()
    }

    /**
     * Split the text by delimiter, and keep the nth element(0 is the first).
     */
    fun d2_split(input: String?, delimiter: String?, index: Int?): String? {
        if (input == null || delimiter == null) return ""
        val parts = input.split(Regex.escape(delimiter).toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return if (index == null || index < 0 || index >= parts.size) "" else parts[index]
    }

    fun d2_substring(input: String?, beginIndex: Int?, endIndex: Int?): String? {
        return input?.substring(beginIndex?.coerceAtLeast(0)?: 0, endIndex?.coerceAtMost(input.length)?: input.length) ?: ""
    }

    fun d2_validatePattern(input: String?, regex: String?): Boolean {
        return input != null && regex != null && input.matches(regex.toRegex())
    }

    fun d2_weeksBetween(start: LocalDate?, end: LocalDate?): Int {
        require(start != null) { "start parameter of d2:weeksBetween must not be null" }
        require(end != null) { "end parameter of d2:weeksBetween must not be null" }
        return d2_daysBetween(start, end) / 7
    }

    fun d2_yearsBetween(start: LocalDate?, end: LocalDate?): Int {
        require(start != null) { "start parameter of d2:yearsBetween must not be null" }
        require(end != null) { "end parameter of d2:yearsBetween must not be null" }
        return if (start.toEpochDays() < end.toEpochDays()) start.yearsUntil(end) else end.yearsUntil(start)
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
    fun d2_zpvc(values: List<Number?>): Int {
        return values.count { n -> n != null && n.toDouble() >= 0.0 }
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
