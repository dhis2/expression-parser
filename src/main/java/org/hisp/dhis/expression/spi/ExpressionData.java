package org.hisp.dhis.expression.spi;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

/**
 * The value context used during expression evaluation.
 *
 * These are the values plugged into the expression for data items, variables or the general context.
 */
@Value
@Builder(toBuilder = true)
public class ExpressionData {

    @Builder.Default
    Map<String, ? extends VariableValue> programRuleVariableValues = Map.of();
    @Builder.Default
    Map<String, Object> programVariableValues = Map.of();
    @Builder.Default
    Map<String, List<String>> supplementaryValues = Map.of();
    @Builder.Default
    Map<DataItem, Object> dataItemValues = Map.of();
    @Builder.Default
    Map<String, Object> namedValues = Map.of();
}
