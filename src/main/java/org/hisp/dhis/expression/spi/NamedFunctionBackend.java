package org.hisp.dhis.expression.spi;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;

/**
 * Backend for all {@link org.hisp.dhis.expression.ast.NamedFunction}s.
 *
 * All are implemented as default methods to be overridden by an actual implementation if needed.
 *
 * @author Jan Bernitt
 */
public interface NamedFunctionBackend {

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

    default Double log(Number base, Number n) {
        return n.doubleValue(); //TODO
    }

    default Double log10(Number n)
    {
        return Math.log10(n.doubleValue());
    }

    default Double avg(List<Double> values) {
        return null;
    }
}
