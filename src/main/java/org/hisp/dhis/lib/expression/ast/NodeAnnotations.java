package org.hisp.dhis.lib.expression.ast;

import java.util.List;

/**
 * Optional information in a {@link Node} that is provided in a mutable way using setters.
 *
 * @author Jan Bernitt
 */
public interface NodeAnnotations {

    /**
     * After a node has been created using beginNode and endNode the surrounding
     * whitespace can be attached to the most recent created node.
     * <p>
     * The list of {@link String} contains the sequences of whitespace surrounding the node's syntax.
     * In a simple case like a number literal this would be the whitespace before and after the node syntax.
     * A complex syntax with arguments would have the whitespace before it, between the argument list and after it.
     * <p>
     * The whitespace can be used to realign it with generated syntax output by its index.
     *
     * @param wsTokens list of whitespace for the node. Might contain empty strings but never {@code null}
     */
    void setWsTokens(List<String> wsTokens);

    /**
     * @return The whitespace between the syntax elements of this node or an empty list if no whitespace was attached
     */
    List<String> getWsTokens();

    Position getStart();

    void setStart(Position start);

    Position getEnd();

    void setEnd(Position end);
}
