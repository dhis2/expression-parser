package org.hisp.dhis.lib.expression.spi;

import lombok.Value;
import org.hisp.dhis.lib.expression.ast.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.hisp.dhis.lib.expression.eval.Evaluate.normalise;

@Value
public class Issues {

    List<Issue> errors = new ArrayList<>();
    List<Issue> warnings = new ArrayList<>();

    public void throwIfErrorsOrWarnings() {
        if (!errors.isEmpty() || !warnings.isEmpty()) {
            throw new IllegalExpressionException(errors, warnings);
        }
    }

    public void addError(Node<?> node, String message) {
        errors.add(new Issue(position(node), message));
    }

    public void addWarning(Node<?> node, String message) {
        warnings.add(new Issue(position(node), message));
    }

    public void addIssue(boolean warning, Node<?> node, String message) {
        if (warning) {
            addWarning(node, message);
        } else {
            addError(node, message);
        }
    }

    private static Supplier<String> position(Node<?> node) {
        return () -> normalise(node);
    }
}
