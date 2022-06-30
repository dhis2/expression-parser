package org.hisp.dhis.expression;

import java.time.LocalDateTime;

public class DescriptionTreeWalker implements NodeVisitor {

    private final StringBuilder out = new StringBuilder();

    @Override
    public String toString() {
        return out.toString();
    }

    @Override
    public void visitParentheses(Node<Void> group) {
        out.append('(');
        group.forEachChild(child -> child.walk(this));
        out.append(')');
    }

    @Override
    public void visitArgument(Node<Integer> argument) {
        boolean wasUid = false;
        for (int i = 0; i < argument.size(); i++)
        {
            Node<?> child = argument.child(i);
            if (wasUid && child.getType() == NodeType.UID) out.append('&');
            child.walk(this);
            wasUid = child.getType() == NodeType.UID;
        }
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
        for (int i = 0; i < function.size(); i++)
        {
            if (i > 0) out.append(',');
            function.child(i).walk(this);
        }
        out.append(')');
    }

    @Override
    public void visitMethod(Node<NamedMethod> method) {
        out.append('.').append(method.getValue().name()).append('(');
        for (int i = 0; i < method.size(); i++)
        {
            if (i > 0) out.append(',');
            method.child(i).walk(this);
        }
        out.append(')');
    }

    @Override
    public void visitDataValue(Node<DataValue> data) {
        Node<?> c0 = data.child(0);
        if (c0.getType() == NodeType.STRING && data.size() == 1) {
            // programRuleStringVariableName
            c0.walk(this);
            return;
        }
        boolean isPS_EVENTDATE = data.getValue() == DataValue.DATA_ELEMENT && c0.child(0).getValue() == Tag.PS_EVENTDATE;
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
        Double d = value.getValue();
        if (d % 1.0d == 0d) {
            out.append(d.intValue());
        } else {
            out.append(d);
        }
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
    public void visitWildcard(Node<Void> value) {
        out.append("*");
    }

    @Override
    public void visitString(Node<String> value) {
        //TODO reverse escape
        out.append("'").append(value.getValue()).append("'");
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
