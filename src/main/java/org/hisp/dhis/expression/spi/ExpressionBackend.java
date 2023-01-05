package org.hisp.dhis.expression.spi;

import org.hisp.dhis.expression.ast.NamedValue;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;

/**
 * Joins all required backends of the expression languages as {@link ExpressionBackend}.
 *
 * The backend connect named functions, modifiers and constants/values to their actual implementation or actual value.
 *
 * @author Jan Bernitt
 */
@FunctionalInterface
public interface ExpressionBackend {

    Object namedValue(NamedValue key);

    /**
     * Returns first non-null value (of similar typed values).
     *
     * @param values zero or more values
     * @return the first value that is not null, or null if all values are zero or values is of length zero
     */
    default Object firstNonNull(List<?> values)
    {
        return values.stream().filter(Objects::nonNull).findFirst().orElse(null);
    }

    /**
     * Returns the largest of the values.
     *
     * @param values zero or more values, nulls allowed (ignored)
     * @return maximum value or null if all values are null
     */
    default Number greatest(Number...values)
    {
        return Stream.of(values).filter(Objects::nonNull).max(comparing(Number::doubleValue)).orElse(null);
    }

    /**
     * Returns conditional value based on the condition.
     * Actually not the equivalent of an if-else block but the ternary operator {@code condition ? ifValue : elseValue}.
     *
     * @param condition test, maybe null
     * @param ifValue value when condition is true
     * @param elseValue value when condition if false or null
     * @return either if value or else value based on the condition
     * @param <T> type of value
     */
    default <T> T ifThenElse(Boolean condition, T ifValue, T elseValue)
    {
        return Boolean.TRUE.equals(condition) ? ifValue : elseValue;
    }

    default boolean isNotNull(Object value) {
        return Objects.nonNull(value);
    }

    default boolean isNull(Object value) {
        return Objects.isNull(value);
    }

    /**
     * Returns the smallest of the values
     *
     * @param values zero or more values, nulls allowed (ignored)
     * @return minimum value or null if all values are null
     */
    default Number least(Number... values) {
        return Stream.of(values).filter(Objects::nonNull).min(comparing(Number::doubleValue)).orElse(null);
    }

    default double log( Number n ) {
        return Math.log(n.doubleValue());
    }

    default double log10(Number n)
    {
        return Math.log10(n.doubleValue());
    }

    /*
    Aggregate functions...
     */

    default double avg(double[] values) {
        return AggregateMath.avg(values);
    }
    default double count(double[] values) {
        return AggregateMath.count(values);
    }
    default double max(double[] values) {
        return AggregateMath.max(values);
    }
    default double median(double[] values) {
        return AggregateMath.median(values);
    }
    default double min(double[] values) {
        return AggregateMath.min(values);
    }
    default Double percentileCont(double[] values, Number fraction) {
        return AggregateMath.percentileCont(values, fraction);
    }
    default double stddev(double[] values) {
        return AggregateMath.stddev(values);
    }
    default double stddevPop(double[] values) {
        return AggregateMath.stddevPop(values);
    }
    default double stddevSamp(double[] values) {
        return AggregateMath.stddevSamp(values);
    }
    default double sum(double[] values) {
        return AggregateMath.sum(values);
    }
    default double variance(double[] values) {
        return AggregateMath.variance(values);
    }
}
