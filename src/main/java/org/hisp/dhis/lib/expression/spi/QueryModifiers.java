package org.hisp.dhis.lib.expression.spi;

import lombok.Builder;
import lombok.Value;
import org.hisp.dhis.lib.expression.ast.AggregationType;

import java.time.LocalDate;
import java.util.function.BiConsumer;

@Value
@Builder(toBuilder = true)
public class QueryModifiers {

    /**
     * Use aggregation over periods when loading data.
     * Value then must be a {@code double[]}.
     */
    boolean periodAggregation;
    AggregationType aggregationType;
    LocalDate maxDate;
    LocalDate minDate;
    Integer periodOffset;
    Integer stageOffset;
    boolean yearToDate;
    String subExpression; // SQL

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        BiConsumer<String,Object> toStr = (name, value) -> {
            if (value != null && value != Boolean.FALSE) {
                str.append(".").append(name).append("(").append(value == Boolean.TRUE ? "" : value.toString()).append(")");
            }
        };
        toStr.accept("periodAggregation", periodAggregation);
        toStr.accept("aggregationType", aggregationType);
        toStr.accept("maxDate", maxDate);
        toStr.accept("minDate", minDate);
        toStr.accept("periodOffset", periodOffset);
        toStr.accept("stageOffset", stageOffset);
        toStr.accept("yearToDate", yearToDate);
        return str.toString();
    }
}
