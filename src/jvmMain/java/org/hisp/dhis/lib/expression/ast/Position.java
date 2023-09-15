package org.hisp.dhis.lib.expression.ast;

import org.hisp.dhis.lib.expression.ast.NodeAnnotations.Whitespace;

import java.util.List;

/**
 * Information about positions in the input.
 *
 * @param charIndex  The absolute character index for the input expression
 * @param spaceIndex The absolute whitespace token count (so far). A single WS token is one or more successive WS
 *                   characters also including comments.
 * @author Jan Bernitt
 */
public record Position(int charIndex, int spaceIndex) {

    public Position offsetBy(int charOffset) {
        return new Position(charIndex + charOffset, spaceIndex);
    }

    /**
     * Adds whitespace to each node based on the {@link Position} information.
     *
     * @param root     the node that is the effective root for the provided list of whitespace tokens
     * @param wsTokens the sequence of whitespace tokens for the entire expression
     */
    public static void addWhitespace(Node<?> root, List<String> wsTokens) {
        addWhitespace(root, wsTokens, 0, wsTokens.size());
    }

    private static void addWhitespace(Node<?> node, List<String> wsTokens, int spaceIndex0, int spaceIndexN) {
        int size = node.size();
        if (size == 0) return;
        for (int i = 0; i < size; i++) {
            Node<?> child = node.child(i);
            int selfStartIndex = child.getStart().spaceIndex();
            int selfEndIndex = child.getEnd().spaceIndex();
            int prevEndIndex = i == 0 ? spaceIndex0 : node.child(i - 1).getEnd().spaceIndex();
            int nextStartIndex = size == 1 || i == size - 1 ? spaceIndexN : node.child(i + 1).getStart().spaceIndex();
            int deltaBefore = selfStartIndex - prevEndIndex;
            int deltaAfter = nextStartIndex - selfEndIndex;
            boolean hasAfter = deltaAfter > 1 || deltaAfter == 1 && i == size - 1;
            child.setWhitespace(Whitespace.of(
                    deltaBefore > 0 ? wsTokens.get(selfStartIndex - 1) : "",
                    hasAfter && selfEndIndex < wsTokens.size() ? wsTokens.get(selfEndIndex) : ""));
            addWhitespace(child, wsTokens, selfStartIndex, selfEndIndex);
        }
    }

}