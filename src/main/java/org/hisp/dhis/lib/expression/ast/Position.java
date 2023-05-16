package org.hisp.dhis.lib.expression.ast;

/**
 * Information about positions in the input.
 */
public final class Position {

    /**
     * The absolute character index
     */
    public final int inputChar;
    /**
     * The absolute whitespace token count (so far)
     */
    public final int wsToken;

    public Position(int inputChar, int wsToken) {
        this.inputChar = inputChar;
        this.wsToken = wsToken;
    }

    public Position offsetBy(int inputCharOffset) {
        return new Position(inputChar+inputCharOffset, wsToken);
    }
}
