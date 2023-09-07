package org.hisp.dhis.lib.expression.spi;

import lombok.Getter;

import java.util.List;

import static java.lang.String.format;

@Getter
public class IllegalExpressionException extends IllegalArgumentException {

    private final transient List<Issue> errors;
    private final transient List<Issue> warnings;

    public IllegalExpressionException(String message) {
        super(message);
        this.errors = List.of(new Issue(() -> "", message));
        this.warnings = List.of();
    }

    public IllegalExpressionException(List<Issue> errors, List<Issue> warnings) {
        super(format("%d error(s), %d warning(s)", errors.size(), warnings.size()));
        this.errors = errors;
        this.warnings = warnings;
    }
}
