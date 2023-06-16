package org.hisp.dhis.lib.expression.spi;

import lombok.Value;

import java.util.function.Supplier;

@Value
public class Issue {
    Supplier<String> position;
    String message;
}
