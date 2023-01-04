package org.hisp.dhis.expression.util;

import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.ast.NodeType;
import org.hisp.dhis.expression.parse.NonTerminal;
import org.hisp.dhis.expression.parse.ParseContext;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DebugParseContext implements ParseContext {

    private final List<NonTerminal> fragments;
    private final Appendable out;
    private final Set<NodeType> filter;
    private final LinkedList<NodeType> stack = new LinkedList<>();
    private String indent = "";

    public DebugParseContext(List<NonTerminal> fragments, Appendable out) {
        this(fragments, out, EnumSet.of(NodeType.ARGUMENT));
    }
    public DebugParseContext(List<NonTerminal> fragments, Appendable out, Set<NodeType> filter ) {
        this.fragments = fragments;
        this.out = out;
        this.filter = filter;
    }

    @Override
    public NonTerminal fragment(String name) {
        return fragments.stream().filter(f -> f.name().equals(name)).findFirst().orElse(null);
    }

    @Override
    public void beginNode(NodeType type, String value, Node.Factory create) {
        if (!filter.contains(type)) {
            try {
                out.append(indent).append(type.name()).append("=").append(value).append('\n');
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
