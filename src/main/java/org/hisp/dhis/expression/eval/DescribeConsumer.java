package org.hisp.dhis.expression.eval;

import org.hisp.dhis.expression.ast.BinaryOperator;
import org.hisp.dhis.expression.ast.DataItemModifier;
import org.hisp.dhis.expression.ast.NamedFunction;
import org.hisp.dhis.expression.ast.NamedValue;
import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.ast.NodeType;
import org.hisp.dhis.expression.ast.Tag;
import org.hisp.dhis.expression.ast.UnaryOperator;
import org.hisp.dhis.expression.ast.VariableType;
import org.hisp.dhis.expression.spi.DataItem;
import org.hisp.dhis.expression.spi.DataItemType;
import org.hisp.dhis.expression.spi.ID;

import java.time.LocalDate;
import java.util.Map;

/**
 * Converts an AST back into a "normalised" {@link String} form.
 *
 * @author Jan Bernitt
 */
class DescribeConsumer implements NodeVisitor {

    public static String toNormalisedExpression(Node<?> root)
    {
        return toValueExpression(root, Map.of());
    }

    public static String toValueExpression(Node<?> root, Map<DataItem, Number> dataItemValues)
    {
        DescribeConsumer walker = new DescribeConsumer(dataItemValues, Map.of());
        root.walk(walker);
        return walker.toString();
    }

    public static String toDisplayExpression(Node<?> root, Map<String, String> displayNames)
    {
        DescribeConsumer walker = new DescribeConsumer(Map.of(), displayNames);
        root.walk(walker);
        return walker.toString();
    }

    private final StringBuilder out = new StringBuilder();
    private final Map<DataItem, Number> dataItemValues;
    private final Map<String, String> displayNames;

    private int currentDataItemCardinality;
    private DataItem currentDataItem;
    private int currentDataItemIdIndex;

    public DescribeConsumer(Map<DataItem, Number> dataItemValues, Map<String, String> displayNames) {
        this.dataItemValues = dataItemValues;
        this.displayNames = displayNames;
    }

    @Override
    public String toString() {
        return out.toString();
    }

    @Override
    public void visitParentheses(Node<Void> group) {
        out.append('(');
        group.walkChildren(this, null);
        out.append(')');
    }

    @Override
    public void visitArgument(Node<Integer> argument) {
        argument.walkChildren(this,
                (c1,c2) -> out.append(c1.getType() == NodeType.UID && c2.getType() == NodeType.UID ? "&" : ""));
    }

    @Override
    public void visitBinaryOperator(Node<BinaryOperator> operator) {
        operator.child(0).walk(this);
        out.append(operator.getValue().getSymbol());
        operator.child(1).walk(this);
    }

    @Override
    public void visitUnaryOperator(Node<UnaryOperator> operator) {
        out.append(operator.getValue().getSymbol());
        operator.child(0).walk(this);
    }

    @Override
    public void visitFunction(Node<NamedFunction> function) {
        out.append(function.getValue().getName()).append('(');
        function.walkChildren(this, (c1,c2) -> out.append(','));
        out.append(')');
    }

    @Override
    public void visitModifier(Node<DataItemModifier> modifier) {
        out.append('.').append(modifier.getValue().name()).append('(');
        modifier.walkChildren(this, (c1,c2) -> out.append(','));
        out.append(')');
    }

    @Override
    public void visitDataItem(Node<DataItemType> item) {
        currentDataItemCardinality = item.size();
        currentDataItem = item.toDataItem();
        Number value = dataItemValues.get(currentDataItem);
        if (value != null) {
            out.append(value);
        } else {
            describeDataItem(item);
        }
    }

    private void describeDataItem(Node<DataItemType> item) {
        Node<?> c0 = item.child(0);
        boolean isPS_EVENTDATE = c0.child(0).getValue() == Tag.PS_EVENTDATE;
        if (!isPS_EVENTDATE) {
            out.append(item.getValue().getSymbol());
            out.append('{');
        }
        for (int i = 0; i < item.size(); i++)
        {
            if (i > 0) out.append('.');
            currentDataItemIdIndex = i;
            item.child(i).walk(this);
        }
        if (!isPS_EVENTDATE) {
            out.append('}');
        }
        visitModifiers(item);
    }

    private void visitModifiers(Node<?> item) {
        for (Node<?> mod : item.modifiers()) {
            if (mod.getValue() != DataItemModifier.periodAggregation) {
                mod.walk(this);
            }
        }
    }

    @Override
    public void visitVariable(Node<VariableType> variable) {
        if (!displayNames.isEmpty()) {
            String name = displayNames.get(variable.child(0).getRawValue());
            if (name != null) {
                out.append(name);
                return;
            }
        }
        out.append(variable.getRawValue());
        out.append('{');
        variable.child(0).walk(this);
        out.append('}');
        visitModifiers(variable);
    }

    @Override
    public void visitNamedValue(Node<NamedValue> value) {
        out.append('[').append(value.getValue().name()).append(']');
    }

    @Override
    public void visitNumber(Node<Double> value) {
        out.append(value.getRawValue() );
    }

    @Override
    public void visitInteger(Node<Integer> value) {
        out.append(value.getValue());
    }

    @Override
    public void visitBoolean(Node<Boolean> value) {
        out.append(value.getValue());
    }

    @Override
    public void visitNull(Node<Void> value) {
        out.append("null");
    }

    @Override
    public void visitString(Node<String> value) {
        out.append("'").append(value.getRawValue()).append("'");
    }

    @Override
    public void visitIdentifier(Node<?> value) {
        out.append(value.getRawValue());
        if (value.getValue() instanceof Tag)
            out.append(':');
    }

    @Override
    public void visitUid(Node<String> value) {
        if (!displayNames.isEmpty()) {
            ID id = new ID(currentDataItem.getType().getType(currentDataItemCardinality, currentDataItemIdIndex), value.getValue());
            String name = displayNames.get(value.getValue());
            if (name != null) {
                out.append(name);
                return;
            }
        }
        out.append(value.getValue());
    }

    @Override
    public void visitDate(Node<LocalDate> value) {
        out.append(value.getValue());
    }
}
