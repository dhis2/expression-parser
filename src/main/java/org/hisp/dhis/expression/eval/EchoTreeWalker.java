package org.hisp.dhis.expression.eval;

import org.hisp.dhis.expression.ast.BinaryOperator;
import org.hisp.dhis.expression.spi.DataItemType;
import org.hisp.dhis.expression.ast.NamedFunction;
import org.hisp.dhis.expression.ast.DataItemModifier;
import org.hisp.dhis.expression.ast.NamedValue;
import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.ast.NodeType;
import org.hisp.dhis.expression.ast.Tag;
import org.hisp.dhis.expression.ast.UnaryOperator;

import java.time.LocalDateTime;

public class EchoTreeWalker implements NodeVisitor {

    public static String toExpression(Node<?> node)
    {
        EchoTreeWalker walker = new EchoTreeWalker();
        node.walk(walker);
        return walker.toString();
    }

    private final StringBuilder out = new StringBuilder();

    @Override
    public String toString() {
        return out.toString();
    }

    @Override
    public void visitParentheses(Node<Void> group) {
        boolean root = out.length() > 0;
        if (root)
            out.append('(');
        group.walkChildren(this, null);
        if (root)
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
    public void visitDataItem(Node<DataItemType> data) {
        Node<?> c0 = data.child(0);
        if (data.size() == 1 && (c0.getType() == NodeType.STRING || c0.getType() == NodeType.IDENTIFIER)) {
            // programRuleStringVariableName
            c0.walk(this);
            return;
        }
        boolean isPS_EVENTDATE = c0.child(0).getValue() == Tag.PS_EVENTDATE;
        if (!isPS_EVENTDATE) {
            out.append(data.getValue().getSymbol());
            out.append('{');
        }
        for (int i = 0; i < data.size(); i++)
        {
            if (i > 0) out.append('.');
            data.child(i).walk(this);
        }
        if (!isPS_EVENTDATE) {
            out.append('}');
        }
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
        out.append(value.getValue());
    }

    @Override
    public void visitDate(Node<LocalDateTime> value) {
        out.append(value.getValue());
    }
}
