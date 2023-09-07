package org.hisp.dhis.lib.expression.util;

import lombok.Value;
import org.hisp.dhis.lib.expression.ast.Node;
import org.hisp.dhis.lib.expression.ast.NodeType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * A simplified tree structure useful to verify the {@link NodeType} structure a {@link Node} AST.
 *
 * @author Jan Bernitt
 */
@Value(staticConstructor = "of")
public class TNode {

    NodeType type;
    List<TNode> children;

    public static TNode ofLevels(NodeType root, NodeType... level1toN) {
        TNode node = TNode.of(root);
        TNode target = node;
        for (NodeType t : level1toN) {
            TNode c = of(t);
            target.children.add(c);
            target = c;
        }
        return node;
    }

    public static TNode of(NodeType type) {
        return new TNode(type, new ArrayList<>());
    }

    public static TNode of(Node<?> root) {
        return root.map(Node::getType, TNode::new);
    }

    public TNode add(NodeType type) {
        children.add(of(type));
        return this;
    }

    public TNode add(NodeType type, UnaryOperator<TNode> with) {
        children.add(with.apply(of(type)));
        return this;
    }

    public TNode add(NodeType type, NodeType... level1toN) {
        children.add(ofLevels(type, level1toN));
        return this;
    }
}
