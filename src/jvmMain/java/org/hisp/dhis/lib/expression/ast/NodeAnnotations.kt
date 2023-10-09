package org.hisp.dhis.lib.expression.ast

/**
 * Optional information in a [Node] that is provided in a mutable way using setters.
 *
 * @author Jan Bernitt
 */
interface NodeAnnotations {
    @JvmRecord
    data class Whitespace(@JvmField val before: String, @JvmField val after: String) {
        fun before(ifDefault: String): String {
            return if (this === DEFAULT) ifDefault else before
        }

        fun after(ifDefault: String): String {
            return if (this === DEFAULT) ifDefault else after
        }

        companion object {
            @JvmField
            val DEFAULT = Whitespace("", "")
            val NONE = Whitespace("", "")
            @JvmStatic
            fun of(before: String, after: String): Whitespace {
                return if (before.isEmpty() && after.isEmpty()) NONE else Whitespace(before, after)
            }
        }
    }

    /**
     * After a node has been created using beginNode and endNode the surrounding whitespace can be attached to the most
     * recent created node.
     *
     *
     * The list of [String] contains the sequences of whitespace surrounding the node's syntax. In a simple case
     * like a number literal this would be the whitespace before and after the node syntax. A complex syntax with
     * arguments would have the whitespace before it, between the argument list and after it.
     *
     *
     * The whitespace can be used to realign it with generated syntax output by its index.
     *
     * @param whitespace list of whitespace for the node. Might contain empty strings but never `null`
     */
    fun setWhitespace(whitespace: Whitespace)

    /**
     * @return The whitespace between the syntax elements of this node or an empty list if no whitespace was attached
     */
    fun getWhitespace(): Whitespace

    fun getStart(): Position?

    fun setStart(start: Position?)

    fun getEnd(): Position?

    fun setEnd(end: Position?)
}
