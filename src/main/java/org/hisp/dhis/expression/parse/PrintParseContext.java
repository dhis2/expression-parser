package org.hisp.dhis.expression.parse;

import org.hisp.dhis.expression.Node;
import org.hisp.dhis.expression.NodeType;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PrintParseContext extends AbstractParseContext {

    private String indent = "";
    private LinkedList<NodeType> stack = new LinkedList<>();

    public PrintParseContext(List<NonTerminal> constants, List<NonTerminal> functions, List<NonTerminal> methods) {
        super(constants, functions, methods);
    }

    private Map<String, NonTerminal> mapByName(List<NonTerminal> functions) {
        return functions.stream().collect(Collectors.toUnmodifiableMap(NonTerminal::name, Function.identity()));
    }

    @Override
    public void beginNode(NodeType type, String value, Node.Factory create) {
        if (true ||type != NodeType.ARGUMENT)
        System.out.println(indent+type+"="+value);
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
