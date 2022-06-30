package org.hisp.dhis.expression.parse;

import org.hisp.dhis.expression.Node;
import org.hisp.dhis.expression.NodeType;
import org.hisp.dhis.expression.Nodes;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A {@link ParseContext} that builds a {@link Node}-tree using {@link Node.Factory}.
 *
 * @author Jan Bernitt
 */
public class TreeParseContext extends AbstractParseContext {

    private static final Map<NodeType, Node.Factory> DEFAULT_FACTORIES = new EnumMap<>(NodeType.class);

    static {
        DEFAULT_FACTORIES.put(NodeType.PAR, Nodes.ComplexTextNode::new);
        DEFAULT_FACTORIES.put(NodeType.ARGUMENT, Nodes.ArgumentNode::new);
        DEFAULT_FACTORIES.put(NodeType.FUNCTION, Nodes.ComplexTextNode::new);
        DEFAULT_FACTORIES.put(NodeType.METHOD, Nodes.MethodNode::new);
        DEFAULT_FACTORIES.put(NodeType.DATA_VALUE, Nodes.DataValueNode::new);

        DEFAULT_FACTORIES.put(NodeType.UNARY_OPERATOR, Nodes.UnaryOperatorNode::new);
        DEFAULT_FACTORIES.put(NodeType.BINARY_OPERATOR, Nodes.BinaryOperatorNode::new);

        DEFAULT_FACTORIES.put(NodeType.STRING, Nodes.SimpleTextNode::new);
        DEFAULT_FACTORIES.put(NodeType.NAMED_VALUE, Nodes.SimpleTextNode::new);
        DEFAULT_FACTORIES.put(NodeType.UID, Nodes.SimpleTextNode::new);
        DEFAULT_FACTORIES.put(NodeType.IDENTIFIER, Nodes.SimpleTextNode::new);
        DEFAULT_FACTORIES.put(NodeType.NUMBER, Nodes.NumberNode::new);
        DEFAULT_FACTORIES.put(NodeType.INTEGER, Nodes.IntegerNode::new);
        DEFAULT_FACTORIES.put(NodeType.DATE, Nodes.DateNode::new);
        DEFAULT_FACTORIES.put(NodeType.BOOLEAN, Nodes.BooleanNode::new);
        DEFAULT_FACTORIES.put(NodeType.NULL, Nodes.ConstantNode::new);
        DEFAULT_FACTORIES.put(NodeType.WILDCARD, Nodes.ConstantNode::new);
    }

    public static TreeParseContext withDefaults(List<NonTerminal> constants, List<NonTerminal> functions, List<NonTerminal> methods) {
        return new TreeParseContext(constants, functions, methods, DEFAULT_FACTORIES);
    }

    public static TreeParseContext withDefaultsExcept(List<NonTerminal> constants, List<NonTerminal> functions, List<NonTerminal> methods, Map<NodeType, Node.Factory> factories) {
        EnumMap<NodeType, Node.Factory> merged = new EnumMap<>(NodeType.class);
        merged.putAll(DEFAULT_FACTORIES);
        merged.putAll(factories);
        return new TreeParseContext(constants, functions, methods, merged);
    }

    private final Map<NodeType, Node.Factory> factoryByType;

    private final LinkedList<Node<?>> stack = new LinkedList<>();

    private Node<?> root;

    private TreeParseContext(List<NonTerminal> constants, List<NonTerminal> functions, List<NonTerminal> methods, Map<NodeType, Node.Factory> factoryByType) {
        super(constants, functions, methods);
        this.factoryByType = factoryByType;
    }

    public Node<?> getRoot() {
        return root;
    }

    @Override
    public void beginNode(NodeType type, String value, Node.Factory create) {
        Node.Factory f = create != null ? create : factoryByType.get(type);
        if (f == null)
        {
            throw new UnsupportedOperationException("No factory for type: "+type);
        }
        Node<?> node = f.create(type, value);
        if (stack.isEmpty()) {
            root = new Nodes.ComplexTextNode(NodeType.PAR, "");
            root.addChild(node);
            stack.addLast(root);
        } else {
            stack.getLast().addChild(node);
        }
        stack.addLast(node);
    }

    @Override
    public void endNode(NodeType type) {
        stack.removeLast();
    }
}
