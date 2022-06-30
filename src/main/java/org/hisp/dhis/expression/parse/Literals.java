package org.hisp.dhis.expression.parse;

import org.hisp.dhis.expression.NodeType;

import static java.lang.Integer.parseInt;

public interface Literals {

    static String parse(Expr expr, NodeType type )
    {
        switch ( type )
        {
            case BINARY_OPERATOR:
                return parseBinaryOp(expr);
            case UNARY_OPERATOR:
                return parseUnaryOp(expr);
            case NUMBER:
                return parseNumeric(expr);
            case INTEGER:
                return parseInteger(expr);
            case STRING:
                return parseString(expr);
            case BOOLEAN:
                return parseBoolean(expr);
            case DATE:
                return parseDate(expr);
            case UID:
                return parseUid(expr);
            case WILDCARD:
                return parseWildcard(expr);
            case NAMED_VALUE:
            case IDENTIFIER:
                return parseIdentifier(expr);
            default:
                expr.error( "Not a literal type: " + type );
                return null;
        }
    }

    static String parseUnaryOp(Expr expr) {
        char c = expr.peek();
        if (Chars.isUnaryOperator(c))
        {
            expr.error("unary operator");
        }
        return "" + c;
    }

    static String parseIdentifier(Expr expr) {
        return expr.rawMatch("identifier", Chars::isIdentifier);
    }

    static String parseWildcard(Expr expr) {
        expr.expect('*');
        return "*";
    }

    static String parseString(Expr expr) {
        char cq = expr.peek();
        if (cq != '"' && cq != '\'') {
            expr.error("expected start of string literal");
        }
        expr.gobble();
        char c = expr.peek();
        StringBuilder str = new StringBuilder();
        while (c != Chars.EOF) {
            if (c == '\\') {
                expr.gobble(); // the \
                c = expr.peek();
                if (c == 'u')
                { // uXXXX:
                    expr.gobble(); // the u
                    str.appendCodePoint(parseInt(expr.rawMatch("hex", Chars::isHexDigit, Chars::isHexDigit, Chars::isHexDigit, Chars::isHexDigit), 16));
                } else if (Chars.isDigit(c))
                { // 888:
                    str.appendCodePoint(parseInt(expr.rawMatch("octal", Chars::isOctalDigit, Chars::isOctalDigit, Chars::isOctalDigit), 8));
                } else
                { // escape code
                    expr.gobble(); // the escape code
                    switch (c) {
                        case 'b': str.append('\b'); break;
                        case 't': str.append('\t'); break;
                        case 'n': str.append('\n'); break;
                        case 'f': str.append('\f'); break;
                        case 'r': str.append('\r'); break;
                        default: str.append(c); // this is the escaped character
                    }
                }
            }
            else if (c == '\n' || c == '\r' || c == cq) {
                expr.gobble(); // line break or closing quotes
                return str.toString();
            } else {
                expr.gobble(); // the plain character
                str.append(c);
            }
            c = expr.peek();
        }
        expr.error("unclosed string literal, expected closing "+cq);
        return null;
    }

    static String parseName(Expr expr)
    {
        return expr.rawMatch( "name", Chars::isName );
    }

    static String parseInteger(Expr expr)
    {
        int s = expr.position();
        expr.gobbleIf(Chars::isSignOperator);
        expr.skipWhile(Chars::isDigit);
        return expr.raw(s);
    }

    static String parseNumeric(Expr expr)
    {
        int s = expr.position();
        expr.gobbleIf(Chars::isSignOperator);
        boolean hasInt = Chars.isDigit( expr.peek() );
        if ( hasInt )
        {
            expr.skipWhile( Chars::isDigit);
        }
        if ( !hasInt || expr.peek() == '.' )
        {
            expr.expect( '.' );
            expr.skipWhile( Chars::isDigit);
        }
        char c = expr.peek();
        if ( c == 'e' || c == 'E' )
        {
            expr.gobble(); // e/E
            expr.gobbleIf(Chars::isSignOperator);
            expr.skipWhile( Chars::isDigit);
        }
        return expr.raw( s );
    }

    static String parseBoolean(Expr expr)
    {
        return expr.rawMatch( "boolean", expr.peek() == 't' ? "true" : "false" );
    }

    static String parseDate(Expr expr)
    {
        // [1-9] [0-9] [0-9] [0-9] '-' [0-1]? [0-9] '-' [0-3]? [0-9]
        int s = expr.position();
        expr.expect( "digit", Chars::isDigit);
        expr.expect( "digit", Chars::isDigit);
        expr.expect( "digit", Chars::isDigit);
        expr.expect( "digit", Chars::isDigit);
        expr.expect( '-' );
        expr.expect( "digit", Chars::isDigit);
        if ( expr.peek() != '-' )
        {
            expr.expect( "digit", Chars::isDigit);
        }
        else
        {
            expr.expect( '-' );
        }
        expr.expect( "digit", Chars::isDigit);
        expr.gobbleIf(Chars::isDigit);
        return expr.raw( s );
    }

    static String parseUid(Expr expr)
    {
        Chars.CharPredicate alphaNumeric = Chars::isAlphaNumeric;
        return expr.rawMatch( "uid", Chars::isAlpha,
                alphaNumeric, alphaNumeric, alphaNumeric, alphaNumeric, alphaNumeric,
                alphaNumeric, alphaNumeric, alphaNumeric, alphaNumeric, alphaNumeric);
    }

    static String parseBinaryOp(Expr expr)
    {
        char c = expr.peek();
        int s = expr.position();
        if ( Chars.isArithmeticOperator( c ) )
        { // + - * / % ^
            expr.gobble();
            return expr.raw( s );
        }
        if ( Chars.isLogicOperator( c ) )
        { // && ||
            expr.gobble();
            expr.gobbleIf(Chars::isLogicOperator );
            return expr.raw( s );
        }
        else if ( Chars.isComparisonOperator( c ) )
        { // > < >= <= == != <>
            expr.gobble();
            expr.gobbleIf(Chars::isComparisonOperator);
            return expr.raw( s );
        }
        expr.error( "expected operator" );
        return null;
    }

    static boolean isUid(String s)
    {
        return s.length() == 11 && Chars.isAlpha(s.charAt(0)) && s.chars().allMatch(Chars::isAlphaNumeric);
    }

    static boolean isVarName(String s)
    {
        return s.length() > 0 && s.chars().allMatch(Chars::isVarName);
    }
}
