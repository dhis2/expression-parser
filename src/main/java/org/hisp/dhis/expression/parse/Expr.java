package org.hisp.dhis.expression.parse;

import org.hisp.dhis.expression.ast.NodeType;
import org.hisp.dhis.expression.ast.Nodes;
import org.hisp.dhis.expression.parse.Chars.CharPredicate;

import java.io.Serializable;
import java.util.stream.Stream;

import static org.hisp.dhis.expression.parse.Chars.isUnaryOperator;

/**
 * An {@link Expr} is the fundamental building block of the expression grammar.
 *
 * Aside from the actual {@code expr} block this class also implements the data item parsing
 * as it is too irregular to express it using composition.
 *
 * @author Jan Bernitt
 */
public final class Expr implements Serializable
{
    public void error( String desc )
    {
        error(position(), desc);
    }

    public void error( int start, String desc )
    {
        throw new ParseException( start, this, desc );
    }

    public static class ParseException extends IllegalArgumentException
    {
        final Expr expr;

        ParseException(int start, Expr expr, String msg )
        {
            super( msg + pointer(start, expr));
            this.expr = expr;
        }

        static String pointer(int start, Expr expr) {
            String section = new String(expr.expr);
            return start == expr.pos
                ? "\nat: "+ section + "\n"+" ".repeat(expr.pos+4)+"^"
                : "\nat: "+ section + "\n"+" ".repeat(start+4)+"^"+"-".repeat(Math.max(0, expr.pos-start-2))+"^";
        }
    }

    /*
    Non-Terminals
     */

    public static void expr(Expr expr, ParseContext ctx)
    {
        while (true) {
            expr1(expr, ctx);
            while (expr.peek() == '.' && expr.peek(1, Chars::isLetter))
            { // dot function modifier:
                expr.gobble(); // .
                NamedFragments.lookup(expr, Literals::parseName, ctx.fragments()::lookupModifier).parse(expr, ctx);
                expr.skipWS();
            }
            char c = expr.peek();
            if (c == 'a' && expr.peek("and") && !expr.peek(3, Chars::isIdentifier))
            {
                expr.gobble(3);
                ctx.addNode(NodeType.BINARY_OPERATOR, "&&");
            }
            else if (c == 'o' && expr.peek("or") && !expr.peek(2, Chars::isIdentifier))
            {
                expr.gobble(2);
                ctx.addNode(NodeType.BINARY_OPERATOR, "||");
            }
            else if (Chars.isBinaryOperator(c))
            {
                ctx.addNode(NodeType.BINARY_OPERATOR, expr, Literals::parseBinaryOp);
            } else
            {
                return; // no more binary operators => exit loop
            }
        }
    }

    static void expr1( Expr expr, ParseContext ctx )
    {
        expr.skipWS();
        char c = expr.peek();
        if ( isUnaryOperator( c ) && expr.peek(1, p -> p != '=')
                || c == 'n' && expr.peek("not") && !expr.peek(3, Chars::isIdentifier)
        )
        { // unary operators:
            expr.gobble(c == 'n' ? 3 : 1); // unary op
            ctx.addNode(NodeType.UNARY_OPERATOR, c == 'n' ? "!" : ""+c);
            expr1(expr, ctx);
            return;
        }
        if (c == 'd' && expr.peek("distinct") && !expr.peek(8, Chars::isIdentifier))
        {
            expr.gobble(8);
            ctx.addNode(NodeType.UNARY_OPERATOR, "distinct");
            expr1(expr, ctx);
            return;
        }
        if ( c == '(' )
        {
            expr.gobble();
            ctx.beginNode(NodeType.PAR, "");
            expr( expr, ctx );
            ctx.endNode(NodeType.PAR);
            expr.skipWS();
            expr.expect( ')' );
            expr.skipWS();
            return;
        }
        if (c == '[')
        {
            expr.gobble();
            ctx.addNode(NodeType.NAMED_VALUE, Nodes.NamedValueNode::new, expr, Literals::parseIdentifier);
            expr.expect(']');
            expr.skipWS();
            return;
        }
        if (c == '\'' || c == '"')
        { // string literal:
            ctx.addNode(NodeType.STRING, expr, Literals::parseString);
            expr.skipWS();
            return;
        }
        if (c == '.' && expr.peek(1, Chars::isDigit) || Chars.isDigit(c))
        { // numeric literal
            ctx.addNode(NodeType.NUMBER, expr, Literals::parseNumeric);
            expr.skipWS();
            return;
        }
        // should be a top level function or constant then...
        NamedFragments.lookup(expr, Literals::parseName, name -> expr.peek() != '(' && expr.peek() != '{'
                ? ctx.fragments().lookupConstant(name)
                : ctx.fragments().lookupFunction( name )).parse( expr, ctx );
        expr.skipWS();
    }

    /**
     * Data item as it can occur on top level.
     *
     * This method only parses the inner expression between the curly braces.
     */
    static void dataItem(Expr expr, ParseContext ctx) {
        String raw = expr.rawMatch("data item", ce -> ce != '}');
        String[] parts = raw.split("\\.");
        if (Stream.of(parts).allMatch(Expr::isTaggedUidGroup)) {
            for (int i = 0; i < parts.length; i++)
            {
                String part = parts[i];
                int nameEndPos = part.indexOf(':');
                ctx.beginNode(NodeType.ARGUMENT,  ""+i);
                if(nameEndPos > 0)
                {
                    ctx.addNode(NodeType.IDENTIFIER, Nodes.TagNode::new, expr, e -> part.substring(0, nameEndPos));
                }
                Stream.of(part.substring(nameEndPos+1).split("&"))
                        .forEachOrdered(uid -> ctx.addNode(NodeType.UID, uid));
                ctx.endNode(NodeType.ARGUMENT);
            }
        } else if (Literals.isVarName(raw) )
        {
            // programRuleVariableName
            ctx.addNode(NodeType.IDENTIFIER, raw);
        } else
        {
            expr.error("not a valid data item: "+raw);
        }
    }

    static void dataItemInArgumentPosition(Expr expr, ParseContext ctx) {
        char c = expr.peek();
        if (c == '#' || c == 'A') {
            expr.gobble();
            ctx.beginNode(NodeType.DATA_ITEM, ""+c);
            expr.expect('{');
            dataItem(expr, ctx);
            expr.expect('}');
        } else if (c == '"' || c == '\'') {
            // programRuleStringVariableName
            ctx.beginNode(NodeType.DATA_ITEM, "#");
            ctx.addNode(NodeType.STRING, expr, Literals::parseString);
        } else if (c == 'P' && expr.peek("PS_EVENTDATE:")) {
            expr.gobble(13);
            ctx.beginNode(NodeType.DATA_ITEM, "#");
            ctx.beginNode(NodeType.ARGUMENT,  "0");
            ctx.addNode(NodeType.IDENTIFIER, "PS_EVENTDATE", Nodes.TagNode::new);
            expr.skipWS();
            ctx.addNode(NodeType.UID, expr, Literals::parseUid);
            ctx.endNode(NodeType.ARGUMENT);
        } else {
            expr.error("expected data item");
        }
        ctx.endNode(NodeType.DATA_ITEM);
    }

    private static boolean isTaggedUidGroup(String str) {
        str = str.substring(str.indexOf(':')+1); // strip tag
        return Stream.of(str.split("&")).allMatch(s -> s.equals("*") || Literals.isUid(s));
   }

    private final char[] expr;

    private int pos;

    public Expr(String expr)
    {
        this.expr = expr.toCharArray();
        this.pos = 0;
    }

    int position() {
        return pos;
    }

    char peek()
    {
        return pos >= expr.length ? Chars.EOF : expr[pos];
    }

    boolean peek(int ahead, CharPredicate test)
    {
        return pos + ahead < expr.length && test.matches(expr[pos+ahead]);
    }

    boolean peek( String ahead )
    {
        if ( peek() != ahead.charAt( 0 ) )
        {
            return false;
        }
        for ( int i = 1; i < ahead.length(); i++ )
        {
            if ( pos + i >= expr.length || expr[pos + i] != ahead.charAt( i ) )
            {
                return false;
            }
        }
        return true;
    }

    void expect( char c )
    {
        if ( c != peek() )
        {
            error( "expected " + c );
        }
        gobble();
    }

    void expect( String desc, CharPredicate test )
    {
        if ( !test.matches( peek() ) )
        {
            error( "expected " + desc );
        }
        gobble();
    }

    void skipWS()
    {
        skipWhile( Chars::isWS );
    }

    void skipWhile( CharPredicate test )
    {
        while ( peek() != Chars.EOF && test.matches( peek() ) )
        {
            gobble();
        }
    }

    void gobble()
    {
        pos++;
    }

    void gobble(int n)
    {
        pos += n;
    }

    void gobbleIf(CharPredicate test)
    {
        if (test.matches(peek()))
        {
            gobble();
        }
    }

    /*
    Literals
    (A text pattern that is an atomic semantic unit)
     */

    /**
     * Returns the raw input between given start position and the current position
     * @param start start, equal to or before current position
     * @return raw input as string
     */
    String raw(int start )
    {
        return new String( expr, start, pos - start );
    }

    String rawMatch(String desc, CharPredicate test )
    {
        int s = pos;
        skipWhile( test );
        if ( pos == s )
        {
            error( "expected " + desc );
        }
        return raw( s );
    }

    String rawMatch(String desc, CharPredicate... seq)
    {
        int s = pos;
        for ( CharPredicate test : seq )
        {
            if ( !test.matches( peek() ) )
            {
                error( "expected " + desc );
            }
            gobble();
        }
        return raw( s );
    }

    String rawMatch(String desc, String expected )
    {
        for ( int i = 0; i < expected.length(); i++ )
        {
            if ( peek() != expected.charAt( i ) )
            {
                error( "expected " + desc );
            }
            gobble();
        }
        return expected;
    }
}
