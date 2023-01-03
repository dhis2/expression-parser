package org.hisp.dhis.expression.spi;

import lombok.Builder;
import lombok.Value;
import org.hisp.dhis.expression.ast.AggregationType;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class DataItemModifiers {

    boolean periodAggregation;
    AggregationType aggregationType;
    LocalDateTime maxDate;
    LocalDateTime minDate;
    Integer periodOffset;
    Integer stageOffset;
    boolean yearToDate;
}
