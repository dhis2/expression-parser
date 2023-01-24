package org.hisp.dhis.expression.eval;

import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.ast.NodeType;
import org.hisp.dhis.expression.ast.VariableType;
import org.hisp.dhis.expression.spi.DataItem;
import org.hisp.dhis.expression.spi.DataItemType;
import org.hisp.dhis.expression.spi.ExpressionFunctions;
import org.hisp.dhis.expression.spi.ID;
import org.hisp.dhis.expression.spi.IllegalExpressionException;
import org.hisp.dhis.expression.spi.Variable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is the exposed API of the evaluation package.
 * It contains all high level functions to turn a {@link Node}-tree into some value.
 *
 * @author Jan Bernitt
 */
public final class Evaluate {

    private Evaluate() {
        throw new UnsupportedOperationException("util");
    }

    public static Object evaluate(Node<?> root, ExpressionFunctions functions, Map<String, Object> programRuleVariableValues, Map<DataItem, Object> dataItemValues) throws IllegalExpressionException {
        return root.eval(new EvaluateFunction(functions, programRuleVariableValues, dataItemValues));
    }

    public static Set<DataItem> collectDataItems(Node<?> root) {
        return root.aggregate(new HashSet<>(), Node::toDataItem, Set::add, node -> node.getType() == NodeType.DATA_ITEM);
    }

    public static Set<DataItem> collectDataItems(Node<?> root, DataItemType... ofTypes) {
        EnumSet<DataItemType> filter = EnumSet.of(ofTypes[0], ofTypes);
        return root.aggregate(new HashSet<>(), Node::toDataItem, Set::add,
                node -> node.getType() == NodeType.DATA_ITEM && filter.contains(node.getValue()));
    }

    public static Set<String> collectProgramRuleVariables(Node<?> root) {
        return root.aggregate(new HashSet<>(), node -> node.child(0).getRawValue(), Set::add,
                node -> node.getType() == NodeType.VARIABLE && node.getValue() == VariableType.PROGRAM_RULE);
    }

    public static Set<Variable> collectProgramVariables(Node<?> root) {
        return root.aggregate(new HashSet<>(), node -> node.toVariable(), Set::add,
                node -> node.getType() == NodeType.VARIABLE && node.getValue() == VariableType.PROGRAM);
    }

    public static Set<ID> collectUIDs(Node<?> root) {
        return root.aggregate(new HashSet<>(), Node::toIDs,
                (set, ids) -> ids.filter(id -> id.getType().isUID()).forEach(set::add),
                node -> node.getType() == NodeType.DATA_ITEM);
    }

    public static String normalise(Node<?> root) {
        return DescribeConsumer.toExpression(root);
    }

    public static String regenerate(Node<?> root, Map<DataItem, Number> dataItemValues) {
        return DescribeConsumer.toExpression(root, dataItemValues);
    }

    public static String describe(Node<?> root, Map<ID, String> displayNames) {
        return DescribeConsumer.toDisplayExpression(root, displayNames);
    }
}
