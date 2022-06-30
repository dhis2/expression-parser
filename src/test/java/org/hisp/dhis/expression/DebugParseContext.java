package org.hisp.dhis.expression;

import org.hisp.dhis.expression.parse.NamedContext;
import org.hisp.dhis.expression.parse.ParseContext;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Set;

public class DebugParseContext implements ParseContext {

    private final NamedContext named;
    private final Appendable out;
    private final Set<NodeType> filter;
    private final LinkedList<NodeType> stack = new LinkedList<>();
    private String indent = "";

    public DebugParseContext(NamedContext named, Appendable out) {
        this(named, out, EnumSet.of(NodeType.ARGUMENT));
    }
    public DebugParseContext(NamedContext named, Appendable out, Set<NodeType> filter ) {
        this.named = named;
        this.out = out;
        this.filter = filter;
    }

    @Override
    public NamedContext named() {
        return named;
    }

    @Override
    public void beginNode(NodeType type, String value, Node.Factory create) {
        if (!filter.contains(type)) {
            try {
                out.append(indent).append(type.name()).append("=").append(value);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        indent += "  ";
        stack.addLast(type);
    }

    @Override
    public void endNode(NodeType type) {
        indent = indent.substring(2);
        if (stack.peekLast() != type)
            throw new IllegalStateException("Expected ending "+stack.peekLast()+" but ended "+type);
        stack.removeLast();
    }

}
