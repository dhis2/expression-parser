package org.hisp.dhis.expression.parse;

/**
 * Named character sets.
 */
public interface Chars {

    char EOF = 0;

    interface CharPredicate
    {
        boolean matches( char c );
    }

    static boolean isVarName(int c) {
        return isVarName((char)c);
    }

    static boolean isVarName(char c) {
        return isIdentifier(c) || isDigit(c) || c == '-' || c == '.' || isWS(c);
    }

    static boolean isName( char c )
    {
        return c == '#' || c == ':' || c == '_' || c == '.' || isAlphaNumeric( c );
    }

    static boolean isIdentifier(char c )
    {
        return c == '_' || isAlpha( c );
    }

    static boolean isBinaryOperator(char c )
    {
        return isArithmeticOperator( c ) || isLogicOperator( c ) || isComparisonOperator( c );
    }

    static boolean isUnaryOperator(char c)
    {
        return isSignOperator(c) || c == '!';
    }

    static boolean isArithmeticOperator(char c )
    {
        return isSignOperator(c) || c == '*' || c == '/' || c == '%' || c == '^';
    }

    static boolean isSignOperator(char c) {
        return c == '+' || c == '-';
    }

    static boolean isLogicOperator(char c )
    {
        return c == '&' || c == '|';
    }

    static boolean isComparisonOperator(char c )
    {
        return c == '<' || c == '>' || c == '=' || c == '!';
    }

    static boolean isWS(char c )
    {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }

    static boolean isAlpha(char c )
    {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
    }

    static boolean isAlphaNumeric( int c )
    {
        return isAlphaNumeric((char) c);
    }

    static boolean isAlphaNumeric( char c )
    {
        return isAlpha( c ) || isDigit( c );
    }

    static boolean isDigit(char c )
    {
        return c >= '0' && c <= '9';
    }

    static boolean isHexDigit(char c )
    {
        return c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F' || isDigit( c );
    }

    static boolean isOctalDigit(char c)
    {
        return c >= '0' && c <= '7';
    }

}
