package org.hisp.dhis.lib.expression.syntax;

import org.hisp.dhis.lib.expression.ast.Node;
import org.hisp.dhis.lib.expression.ast.NodeType;
import org.hisp.dhis.lib.expression.ast.Nodes;
import org.hisp.dhis.lib.expression.ast.Position;

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

    public static Parser withFragments(List<Fragment> fragments) {
        return new Parser(fragments, new EnumMap<>(DEFAULT_FACTORIES));
    }

    public static Node<?> parse(String expr, List<Fragment> fragments, boolean annotate) {
        Parser parser = Parser.withFragments(fragments);
        List<String> wsTokens = Expr.parse(expr, parser, annotate);
        Node<?> root = parser.getRoot();
        if (annotate) {
            Node.addWsTokens(root, wsTokens);
        }
        Nodes.propagateModifiers(root);
        Node.groupOperators(root);
        return root.getType() == NodeType.PAR && root.size() == 1 ? root.child(0) : root;
    }

    private final Map<String, Fragment> fragmentsByName;
    private final Map<NodeType, Node.Factory> factoriesByType;

    private final LinkedList<Node<?>> stack = new LinkedList<>();

    private Node<?> root;

    private Parser(List<Fragment> fragments, Map<NodeType, Node.Factory> factoriesByType) {
        this.fragmentsByName = mapByName(fragments);
        this.factoriesByType = factoriesByType;
    }

    private static Map<String, Fragment> mapByName(List<Fragment> functions) {
        return functions.stream().collect(toUnmodifiableMap(Fragment::name, Function.identity()));
    }

    public Parser withFragments(Fragment... fragments) {
        this.fragmentsByName.putAll(mapByName(List.of(fragments)));
        return this;
    }

    public Parser withFactory(NodeType type, Node.Factory factory) {
        factoriesByType.put(type, factory);
        return this;
    }

    public Node<?> getRoot() {
        return root;
    }

    @Override
    public Fragment fragment(String name) {
        return fragmentsByName.get(name);
    }

    @Override
    public void beginNode(NodeType type, Position start, String value, Node.Factory create) {
        Node.Factory f = create != null ? create : factoriesByType.get(type);
        if (f == null)
        {
            throw new UnsupportedOperationException("No factory for type: "+type);
        }
        Node<?> node = f.create(type, value);
        node.setStart(start);
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
    public void endNode(NodeType type, Position end) {
        Node<?> node = stack.removeLast();
        node.setEnd(end);
    }
}
