package org.hisp.dhis.lib.expression.eval;

import org.hisp.dhis.lib.expression.spi.DataItem;
import org.hisp.dhis.lib.expression.spi.ID;
import org.hisp.dhis.lib.expression.ast.Node;
import org.hisp.dhis.lib.expression.ast.NodeType;
import org.hisp.dhis.lib.expression.ast.VariableType;
import org.hisp.dhis.lib.expression.spi.DataItemType;
import org.hisp.dhis.lib.expression.spi.ExpressionData;
import org.hisp.dhis.lib.expression.spi.ExpressionFunctions;
import org.hisp.dhis.lib.expression.spi.IllegalExpressionException;
import org.hisp.dhis.lib.expression.spi.Variable;
import org.hisp.dhis.lib.expression.spi.VariableValue;

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

    /*
    Main functions to compute a result
     */

    public static Object evaluate(Node<?> root, ExpressionFunctions functions, ExpressionData data) throws IllegalExpressionException {
        Object value = root.eval(new EvaluateFunction(functions, data));
        return value instanceof VariableValue ? ((VariableValue) value).valueOrDefault() : value;
    }

    public static String normalise(Node<?> root) {
        return DescribeConsumer.toNormalisedExpression(root);
    }

    public static String regenerate(Node<?> root, Map<DataItem, Number> dataItemValues) {
        return DescribeConsumer.toValueExpression(root, dataItemValues);
    }

    public static String describe(Node<?> root, Map<String, String> displayNames) {
        return DescribeConsumer.toDisplayExpression(root, displayNames);
    }

    /*
    Support functions to collect identifiers to supply values to main functions
     */

    public static Set<DataItem> collectDataItems(Node<?> root) {
        return root.aggregate(new HashSet<>(), Node::toDataItem, Set::add, node -> node.getType() == NodeType.DATA_ITEM);
    }

    public static Set<DataItem> collectDataItems(Node<?> root, DataItemType... ofTypes) {
        //TODO need to add subExpression modifier SQL in case that is present
        EnumSet<DataItemType> filter = EnumSet.of(ofTypes[0], ofTypes);
        return root.aggregate(new HashSet<>(), Node::toDataItem, Set::add,
                node -> node.getType() == NodeType.DATA_ITEM && filter.contains(node.getValue()));
    }

    public static Set<String> collectVariableNames(Node<?> root, VariableType type) {
        return root.aggregate(new HashSet<>(), node -> node.child(0).getRawValue(), Set::add,
                node -> node.getType() == NodeType.VARIABLE && node.getValue() == type);
    }

    public static Set<Variable> collectVariables(Node<?> root, VariableType type) {
        return root.aggregate(new HashSet<>(), Node::toVariable, Set::add,
                node -> node.getType() == NodeType.VARIABLE && node.getValue() == type);
    }

    public static Set<ID> collectUIDs(Node<?> root) {
        return root.aggregate(new HashSet<>(), Node::toIDs,
                (set, ids) -> ids.filter(id -> id.getType().isUID()).forEach(set::add),
                node -> node.getType() == NodeType.DATA_ITEM);
    }
}