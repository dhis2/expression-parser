package org.hisp.dhis.lib.expression.ast;

/**
 * Optional information in a {@link Node} that is provided in a mutable way using setters.
 *
 * @author Jan Bernitt
 */
public interface NodeAnnotations {

    record Whitespace(String before, String after){
        public static final Whitespace DEFAULT = new Whitespace("","") ;
        public static final Whitespace NONE = new Whitespace("","") ;

        public static Whitespace of(String before, String after) {
            return before.isEmpty() && after.isEmpty() ? NONE : new Whitespace(before, after);
        }

        public String before(String ifDefault) {
            return this == DEFAULT ? ifDefault : before();
        }

        public String after(String ifDefault) {
            return this == DEFAULT ? ifDefault : after();
        }
    }

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
     * @param whitespace list of whitespace for the node. Might contain empty strings but never {@code null}
     */
    void setWhitespace(Whitespace whitespace);

    /**
     * @return The whitespace between the syntax elements of this node or an empty list if no whitespace was attached
     */
    Whitespace getWhitespace();

    Position getStart();

    void setStart(Position start);

    Position getEnd();

    void setEnd(Position end);
}
