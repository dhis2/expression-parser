package org.hisp.dhis.lib.expression.eval;

import org.hisp.dhis.lib.expression.ast.Node;
import org.hisp.dhis.lib.expression.ast.NodeType;
import org.hisp.dhis.lib.expression.spi.DataItemType;
import org.hisp.dhis.lib.expression.spi.ExpressionData;
import org.hisp.dhis.lib.expression.spi.Issues;

import java.util.List;

import static java.lang.String.format;

/**
 * Validates a {@link Node} using custom logic.
 *
 * @author Jan Bernitt
 */
public interface NodeValidator {

    void validate(Node<?> root, Issues issues, ExpressionData data);

    /*
    Additional AST validation...
     */

    NodeValidator VARIABLE_MUST_EXIST = (root, issues, data) -> root.visit(NodeType.VARIABLE, node -> {
        String name = node.child(0).getRawValue();
        if (!data.getProgramRuleVariableValues().containsKey(name)) {
            issues.addError(node, format("Unknown variable: `%s`", name));
        }
    });

    NodeValidator CONSTANT_MUST_EXIST = (root, issues, data) -> root.visit(NodeType.DATA_ITEM, node -> {
        if (node.getValue() != DataItemType.CONSTANT) return;
        String key = node.toDataItem().getKey();
        if (!data.getProgramRuleVariableValues().containsKey(key)) {
            issues.addError(node, format("Unknown constant: `%s`", key));
        }
    });

    /*
    Modes...
     */

    List<NodeValidator> RuleEngineMode = List.of(
            VARIABLE_MUST_EXIST,
            CONSTANT_MUST_EXIST
    );
}
