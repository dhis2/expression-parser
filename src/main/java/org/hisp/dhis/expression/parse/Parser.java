package org.hisp.dhis.expression.parse;

import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.ast.NodeType;
import org.hisp.dhis.expression.ast.Nodes;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * A {@link ParseContext} that builds a {@link Node}-tree using {@link Node.Factory}.
 *
 * @author Jan Bernitt
 */
public final class Parser implements ParseContext {

    private static final Map<NodeType, Node.Factory> DEFAULT_FACTORIES = new EnumMap<>(NodeType.class);

    static {
        DEFAULT_FACTORIES.put(NodeType.PAR, Nodes.ParenthesesNode::new);
        DEFAULT_FACTORIES.put(NodeType.ARGUMENT, Nodes.ArgumentNode::new);
        DEFAULT_FACTORIES.put(NodeType.FUNCTION, Nodes.FunctionNode::new);
        DEFAULT_FACTORIES.put(NodeType.MODIFIER, Nodes.ModifierNode::new);
        DEFAULT_FACTORIES.put(NodeType.DATA_ITEM, Nodes.DataItemNode::new);

        DEFAULT_FACTORIES.put(NodeType.UNARY_OPERATOR, Nodes.UnaryOperatorNode::new);
        DEFAULT_FACTORIES.put(NodeType.BINARY_OPERATOR, Nodes.BinaryOperatorNode::new);

        DEFAULT_FACTORIES.put(NodeType.STRING, Nodes.Utf8StringNode::new);
        DEFAULT_FACTORIES.put(NodeType.NAMED_VALUE, Nodes.TextNode::new);
        DEFAULT_FACTORIES.put(NodeType.UID, Nodes.TextNode::new);
        DEFAULT_FACTORIES.put(NodeType.IDENTIFIER, Nodes.TextNode::new);
        DEFAULT_FACTORIES.put(NodeType.NUMBER, Nodes.NumberNode::new);
        DEFAULT_FACTORIES.put(NodeType.INTEGER, Nodes.IntegerNode::new);
        DEFAULT_FACTORIES.put(NodeType.DATE, Nodes.DateNode::new);
        DEFAULT_FACTORIES.put(NodeType.BOOLEAN, Nodes.BooleanNode::new);
        DEFAULT_FACTORIES.put(NodeType.NULL, Nodes.ConstantNode::new);
    }

    public static Parser withFragments(NamedFragments fragments) {
        return new Parser(fragments, new EnumMap<>(DEFAULT_FACTORIES));
    }

    public static Node<?> parse(String expr, NamedFragments fragments) {
        Parser parser = Parser.withFragments(fragments);
        Expr.expr(new Expr(expr), parser);
        Node<?> root = parser.getRoot();
        Node.groupOperators(root);
        return root;
    }

    private final NamedFragments fragments;
    private final Map<NodeType, Node.Factory> factoryByType;

    private final LinkedList<Node<?>> stack = new LinkedList<>();

    private Node<?> root;

    private Parser(NamedFragments fragments, Map<NodeType, Node.Factory> factoryByType) {
        this.fragments = fragments;
        this.factoryByType = factoryByType;
    }

    public Parser withFactory(NodeType type, Node.Factory factory) {
        factoryByType.put(type, factory);
        return this;
    }

    public Node<?> getRoot() {
        return root;
    }

    @Override
    public NamedFragments fragments() {
        return fragments;
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
            root = new Nodes.ParenthesesNode(NodeType.PAR, "");
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
