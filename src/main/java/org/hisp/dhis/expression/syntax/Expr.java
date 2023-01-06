package org.hisp.dhis.expression.syntax;

import org.hisp.dhis.expression.ast.NodeType;
import org.hisp.dhis.expression.ast.Nodes;
import org.hisp.dhis.expression.spi.ParseException;
import org.hisp.dhis.expression.syntax.Chars.CharPredicate;

import java.io.Serializable;
import java.util.stream.Stream;

import static org.hisp.dhis.expression.syntax.Chars.isUnaryOperator;

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

    public void error( int pos0, String desc )
    {
        throw new ParseException( formatError(pos0, this, desc) );
    }

    private static String formatError(int pos0, Expr expr, String desc )
    {
        int line = 1;
        int posLine0 = 0;
        for (int p = 0; p < pos0; p++)
            if (expr.expr[p] == '\n') {
                line++;
                posLine0 = p;
            }
        int offset0 = pos0 - posLine0;
        int posLineEnd = posLine0;
        while (posLineEnd < expr.expr.length && expr.expr[posLineEnd] != '\n') posLineEnd++;
        int cutoutLength = Math.min(20, posLineEnd - posLine0);
        String exprCutout = new String(expr.expr, posLine0, cutoutLength  );
        String pointer = expr.pos - pos0 <= 1
                ? " ".repeat(offset0)+"^"
                : " ".repeat(offset0)+"^"+"-".repeat(Math.max(0, expr.pos-pos0-2))+"^";
        return String.format("%s%n\tat line:%d character:%d%n\t%s%n\t%s", desc, line, offset0, exprCutout, pointer);
    }

    /**
     * The root entry point to parse an expression.
     *
     * @param expr the expression to parse
     * @param ctx the parsing context to use to lookup fragments and build the AST
     */
    public static void parse(String expr, ParseContext ctx) {
        expr(new Expr(expr), ctx, true);
    }

    /*
    Non-Terminals
     */

    public static void expr(Expr expr, ParseContext ctx) {
        expr(expr, ctx, false);
    }

    private static void expr(Expr expr, ParseContext ctx, boolean root) {
        while (true) {
            expr1(expr, ctx);
            while (expr.peek() == '.' && expr.peek(1, Chars::isLetter))
            { // a dot modifier:
                expr.gobble(); // .
                FragmentContext.lookup(expr, Literals::parseName, ctx::fragment).parse(expr, ctx);
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
                if (root && expr.pos < expr.expr.length)
                    expr.error("Unexpected input character: '"+expr.peek()+"'");
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
        // should be a named fragment then...
        FragmentContext.lookup(expr, Literals::parseName, ctx::fragment).parse( expr, ctx );
        expr.skipWS();
    }

    /**
     * Entry when data items are arguments to a function
     *
     * One of these:
     * <pre>
     *     #{...}
     *     A{...}
     *     "..."
     *     '...'
     *     PS_EVENTDATE: UID
     * </pre>
     */
    static void dataItem(Expr expr, ParseContext ctx) {
        char c = expr.peek();
        if (c == '#' || c == 'A') {
            expr.gobble(); // #/A
            dataItem(expr, ctx, c);
        } else if (c == '"' || c == '\'') {
            // programRuleStringVariableName
            ctx.beginNode(NodeType.VARIABLE, "");
            ctx.addNode(NodeType.STRING, expr, Literals::parseString);
            ctx.endNode(NodeType.VARIABLE);
        } else if (c == 'P' && expr.peek("PS_EVENTDATE:")) {
            expr.gobble(13);
            ctx.beginNode(NodeType.DATA_ITEM, "#");
            ctx.beginNode(NodeType.ARGUMENT,  "0");
            ctx.addNode(NodeType.IDENTIFIER, "PS_EVENTDATE", Nodes.TagNode::new);
            expr.skipWS();
            ctx.addNode(NodeType.UID, expr, Literals::parseUid);
            ctx.endNode(NodeType.ARGUMENT);
            ctx.endNode(NodeType.DATA_ITEM);
        } else {
            expr.error("Incomplete or malformed value");
        }
    }

    /**
     * Direct entry point when data items are found by name in/via {@code expr}.
     *
     * The name has already been consumed but through the method bound it can be recovered.
     */
    static void dataItemHash(Expr expr, ParseContext ctx) {
        dataItem(expr, ctx, '#');
    }

    /**
     * Direct entry point when data items are found by name in/via {@code expr}.
     *
     * The name has already been consumed but through the method bound it can be recovered.
     */
    static void dataItemA(Expr expr, ParseContext ctx) {
        dataItem(expr, ctx, 'A');
    }

    /**
     * Indirect entry either from data items used top level or as function arguments.
     *
     * At this point the name has been consumed, but it is available from the extra parameter.
     */
    private static void dataItem(Expr expr, ParseContext ctx, char name) {
        expr.expect('{');
        String raw = expr.rawMatch("data item", ce -> ce != '}');
        String[] parts = raw.split("\\.");
        if (Stream.of(parts).allMatch(Expr::isTaggedUidGroup)) {
            ctx.beginNode(NodeType.DATA_ITEM, ""+name);
            // a data item with 1-3 possibly tagged UID groups
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
            ctx.endNode(NodeType.DATA_ITEM);
        } else if (Literals.isVarName(raw) )
        {
            // a programRuleVariableName
            ctx.beginNode(NodeType.VARIABLE, ""+name);
            ctx.addNode(NodeType.IDENTIFIER, raw);
            ctx.endNode(NodeType.VARIABLE);
        } else
        {
            expr.error("Invalid value: '"+raw+"'");
        }
        expr.expect('}');
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
