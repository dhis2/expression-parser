package org.hisp.dhis.lib.expression.eval;

import org.hisp.dhis.lib.expression.ast.*;
import org.hisp.dhis.lib.expression.spi.DataItem;
import org.hisp.dhis.lib.expression.spi.DataItemType;

import java.time.LocalDate;
import java.util.Map;

/**
 * Converts an AST back into a "normalised" or substituted {@link String} form.
 *
 * @author Jan Bernitt
 */
class DescribeConsumer implements NodeVisitor {

    public static String toNormalisedExpression(Node<?> root) {
        return toValueExpression(root, Map.of());
    }

    public static String toValueExpression(Node<?> root, Map<DataItem, Number> dataItemValues) {
        DescribeConsumer walker = new DescribeConsumer(dataItemValues, Map.of());
        root.walk(walker);
        return walker.toString();
    }

    public static String toDisplayExpression(Node<?> root, Map<String, String> displayNames) {
        DescribeConsumer walker = new DescribeConsumer(Map.of(), displayNames);
        root.walk(walker);
        return walker.toString();
    }

    private final StringBuilder out = new StringBuilder();
    private final Map<DataItem, Number> dataItemValues;
    private final Map<String, String> displayNames;

    /**
     * After parsing the root is always a parenthesis that should not be printed. To remember that this was skipped this
     * flag is used because the expression could also start with a {@code (} without that being the root itself.
     */
    private boolean wasRoot;

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
        boolean isRoot = !wasRoot && out.toString().isEmpty();
        if (isRoot) wasRoot = true;
        out.append(group.getWhitespace().before);
        if (!isRoot) out.append('(');
        group.walkChildren(this, null);
        if (!isRoot) out.append(')');
        out.append(group.getWhitespace().after);
    }

    @Override
    public void visitArgument(Node<Integer> argument) {
        out.append(argument.getWhitespace().before);
        argument.walkChildren(this,
                (c1, c2) -> out.append(c1.getType() == NodeType.UID && c2.getType() == NodeType.UID ? "&" : ""));
        out.append(argument.getWhitespace().after);
    }

    @Override
    public void visitBinaryOperator(Node<BinaryOperator> operator) {
        operator.child(0).walk(this);
        out.append(operator.getWhitespace().before(" "));
        out.append(operator.getRawValue());
        out.append(operator.getWhitespace().after(" "));
        operator.child(1).walk(this);
    }

    @Override
    public void visitUnaryOperator(Node<UnaryOperator> operator) {
        String rawValue = operator.getRawValue();
        boolean isWord = Character.isLetter(rawValue.charAt(0));
        String ifDefault = isWord ? " " : "";
        out.append(operator.getWhitespace().before(ifDefault));
        out.append(rawValue);
        out.append(operator.getWhitespace().after(ifDefault));
        operator.child(0).walk(this);
    }

    @Override
    public void visitFunction(Node<NamedFunction> function) {
        out.append(function.getWhitespace().before);
        out.append(function.getValue().getName()).append('(');
        function.walkChildren(this, (c1, c2) -> out.append(','));
        out.append(')');
        out.append(function.getWhitespace().after);
    }

    @Override
    public void visitModifier(Node<DataItemModifier> modifier) {
        out.append(modifier.getWhitespace().before);
        out.append('.').append(modifier.getValue().name()).append('(');
        modifier.walkChildren(this, (c1, c2) -> out.append(','));
        out.append(')');
        out.append(modifier.getWhitespace().after);
    }

    @Override
    public void visitDataItem(Node<DataItemType> item) {
        Number value = dataItemValues.get(item.toDataItem());
        out.append(item.getWhitespace().before);
        if (value != null) {
            out.append(value);
        } else {
            describeDataItem(item);
        }
        out.append(item.getWhitespace().after);
    }

    private void describeDataItem(Node<DataItemType> item) {
        Node<?> c0 = item.child(0);
        boolean isEventDate = c0.child(0).getValue() == Tag.PS_EVENTDATE;
        if (!isEventDate) {
            out.append(item.getValue().getSymbol());
            out.append('{');
        }
        for (int i = 0; i < item.size(); i++) {
            if (i > 0) out.append('.');
            item.child(i).walk(this);
        }
        if (!isEventDate) {
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
        out.append(variable.getWhitespace().before);
        if (!displayNames.isEmpty()) {
            String name = displayNames.get(variable.child(0).getRawValue());
            if (name != null) {
                out.append(name);
                out.append(variable.getWhitespace().after);
                return;
            }
        }
        String symbol = variable.getRawValue();
        boolean hasSymbol = !symbol.isEmpty();
        if (hasSymbol) {
            out.append(symbol);
            out.append('{');
        }
        variable.child(0).walk(this);
        if (hasSymbol) {
            out.append('}');
        }
        visitModifiers(variable);
        out.append(variable.getWhitespace().after);
    }

    @Override
    public void visitNamedValue(Node<NamedValue> value) {
        appendValue(value, "[" + value.getRawValue() + "]");
    }

    @Override
    public void visitNumber(Node<Double> value) {
        appendValue(value, value.getRawValue());
    }

    @Override
    public void visitInteger(Node<Integer> value) {
        appendValue(value, value.getRawValue());
    }

    @Override
    public void visitBoolean(Node<Boolean> value) {
        appendValue(value, value.getRawValue());
    }

    @Override
    public void visitNull(Node<Void> value) {
        appendValue(value, "null");
    }

    @Override
    public void visitString(Node<String> value) {
        appendValue(value, "'" + value.getRawValue() + "'");
    }

    @Override
    public void visitIdentifier(Node<?> value) {
        appendValue(value, value.getRawValue() + (value.getValue() instanceof Tag ? ":" : ""));
    }

    @Override
    public void visitUid(Node<String> value) {
        String uid = value.getValue();
        appendValue(value, displayNames.getOrDefault(uid, uid));
    }

    @Override
    public void visitDate(Node<LocalDate> value) {
        appendValue(value, value.getRawValue());
    }

    private void appendValue(Node<?> node, String value) {
        out.append(node.getWhitespace().before);
        out.append(value);
        out.append(node.getWhitespace().after);
    }
}
