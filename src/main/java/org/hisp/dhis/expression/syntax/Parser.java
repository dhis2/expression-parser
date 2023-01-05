package org.hisp.dhis.expression.syntax;

import org.hisp.dhis.expression.ast.Node;
import org.hisp.dhis.expression.ast.NodeType;
import org.hisp.dhis.expression.ast.Nodes;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toUnmodifiableMap;

/**
 * A {@link ParseContext} that builds a {@link Node}-tree using {@link Node.Factory}.
 *
 * @author Jan Bernitt
 */
public final class Parser implements ParseContext {

    private static final Map<NodeType, Node.Factory> DEFAULT_FACTORIES = new EnumMap<>(NodeType.class);

    static {
        // complex nodes (have children)
        DEFAULT_FACTORIES.put(NodeType.PAR, Nodes.ParenthesesNode::new);
        DEFAULT_FACTORIES.put(NodeType.ARGUMENT, Nodes.ArgumentNode::new);
        DEFAULT_FACTORIES.put(NodeType.FUNCTION, Nodes.FunctionNode::new);
        DEFAULT_FACTORIES.put(NodeType.MODIFIER, Nodes.ModifierNode::new);
        DEFAULT_FACTORIES.put(NodeType.DATA_ITEM, Nodes.DataItemNode::new);
        DEFAULT_FACTORIES.put(NodeType.VARIABLE, Nodes.VariableNode::new);

        DEFAULT_FACTORIES.put(NodeType.UNARY_OPERATOR, Nodes.UnaryOperatorNode::new);
        DEFAULT_FACTORIES.put(NodeType.BINARY_OPERATOR, Nodes.BinaryOperatorNode::new);

        // simple nodes
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

    public static Parser withFragments(List<NonTerminal> fragments) {
        return new Parser(fragments, new EnumMap<>(DEFAULT_FACTORIES));
    }

    public static Node<?> parse(String expr, List<NonTerminal> fragments) {
        Parser parser = Parser.withFragments(fragments);
        Expr.parse(expr, parser);
        Node<?> root = parser.getRoot();
        Node.attachModifiers(root);
        Node.groupOperators(root);
        return root;
    }

    private final Map<String, NonTerminal> fragmentsByName;
    private final Map<NodeType, Node.Factory> factoryByType;

    private final LinkedList<Node<?>> stack = new LinkedList<>();

    private Node<?> root;

    private Parser(List<NonTerminal> fragments, Map<NodeType, Node.Factory> factoryByType) {
        this.fragmentsByName = mapByName(fragments);
        this.factoryByType = factoryByType;
    }

    private static Map<String, NonTerminal> mapByName(List<NonTerminal> functions) {
        return functions.stream().collect(toUnmodifiableMap(NonTerminal::name, Function.identity()));
    }

    public Parser withFragments(NonTerminal... fragments) {
        this.fragmentsByName.putAll(mapByName(List.of(fragments)));
        return this;
    }

    public Parser withFactory(NodeType type, Node.Factory factory) {
        factoryByType.put(type, factory);
        return this;
    }

    public Node<?> getRoot() {
        return root;
    }

    @Override
    public NonTerminal fragment(String name) {
        return fragmentsByName.get(name);
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
