package org.hisp.dhis.lib.expression.ast

import org.hisp.dhis.lib.expression.ast.NodeAnnotations.Whitespace.Companion.of

/**
 * Information about positions in the input.
 *
 * @param charIndex  The absolute character index for the input expression
 * @param spaceIndex The absolute whitespace token count (so far). A single WS token is one or more successive WS
 * characters also including comments.
 * @author Jan Bernitt
 */
data class Position(val charIndex: Int, val spaceIndex: Int) {

    fun offsetBy(charOffset: Int): Position {
        return Position(charIndex + charOffset, spaceIndex)
    }

    companion object {
        /**
         * Adds whitespace to each node based on the [Position] information.
         *
         * @param root     the node that is the effective root for the provided list of whitespace tokens
         * @param wsTokens the sequence of whitespace tokens for the entire expression
         */
        fun addWhitespace(root: Node<*>, wsTokens: List<String>) {
            addWhitespace(root, wsTokens, 0, wsTokens.size)
        }

        private fun addWhitespace(node: Node<*>, wsTokens: List<String>, spaceIndex0: Int, spaceIndexN: Int) {
            val size = node.size()
            if (size == 0) return
            for (i in 0 until size) {
                val child = node.child(i)
                val selfStartIndex = child.getStart()!!.spaceIndex
                val selfEndIndex = child.getEnd()!!.spaceIndex
                val prevEndIndex = if (i == 0) spaceIndex0 else node.child(i - 1).getEnd()!!.spaceIndex
                val nextStartIndex =
                    if (size == 1 || i == size - 1) spaceIndexN else node.child(i + 1).getStart()!!.spaceIndex
                val deltaBefore = selfStartIndex - prevEndIndex
                val deltaAfter = nextStartIndex - selfEndIndex
                val hasAfter = deltaAfter > 1 || deltaAfter == 1 && i == size - 1
                child.setWhitespace(
                    of(
                        if (deltaBefore > 0) wsTokens[selfStartIndex - 1] else "",
                        if (hasAfter && selfEndIndex < wsTokens.size) wsTokens[selfEndIndex] else ""
                    )
                )
                addWhitespace(child, wsTokens, selfStartIndex, selfEndIndex)
            }
        }
    }
}
