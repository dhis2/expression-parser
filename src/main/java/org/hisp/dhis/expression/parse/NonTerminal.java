package org.hisp.dhis.expression.parse;

import org.hisp.dhis.expression.NodeType;

/**
 * A {@link NonTerminal} is a building block of a grammar that consists of further blocks that either are {@link NonTerminal}s themselves or are {@link Terminal}s.
 */
@FunctionalInterface
public interface NonTerminal
{
    /**
     * Parse this non-terminal token for the expr at its current position
     * and use the provided context to emit nodes or lookup named non-terminals.
     *
     * @param expr input to process
     * @param ctx context to build the tree and lookup named non-terminals
     */
    void parse( Expr expr, ParseContext ctx );

    /**
     * @see #maybe()
     *
     * @return true, if this block is an optional argument in an argument list
     */
    default boolean isMaybe()
    {
        return false;
    }

    /**
     * @see #named(String)
     *
     * @return the name of this block as used when doing a lookup for a named block
     */
    default String name()
    {
        return null;
    }


    /**
     * This PEG parser is free of back-tracking.
     * This means a universal "might occur" construct does not exist.
     * The (...)? block here only works in argument lists which will simply check the {@link #isMaybe()} flag.
     * If an expected argument is optional (maybe) an omitted position is not an error but valid and the end of the argument list is found.
     * This also means that optional arguments can only occur at the end of the parameter list, not in the middle.
     *
     * @return This block as optional block (only in argument lists)
     */
    default NonTerminal maybe()
    {
        class Maybe implements NonTerminal {

            private final NonTerminal body;

            public Maybe(NonTerminal body) {
                this.body = body;
            }

            @Override
            public void parse(Expr expr, ParseContext ctx )
            {
                body.parse( expr, ctx );
            }

            @Override
            public boolean isMaybe()
            {
                return true;
            }

            @Override
            public String name() {
                return body.name();
            }
        }
        return this instanceof Maybe ? this : new Maybe(this);
    }

    /**
     * By default, blocks are not named.
     * Naming a block only serves the purpose of indexing the block as a named block for later lookup.
     *
     * Note that a name is a wrapper on the original block
     *
     * @return this block but with a name label attached to it
     */
    default NonTerminal named(String name )
    {
        class Named implements NonTerminal
        {
            final String name;

            private final NonTerminal body;

            Named( String name, NonTerminal body )
            {
                this.name = name;
                this.body = body;
            }

            @Override
            public void parse( Expr expr, ParseContext ctx )
            {
                body.parse( expr, ctx );
            }

            @Override
            public String name()
            {
                return name;
            }

            @Override
            public boolean isMaybe() {
                return body.isMaybe();
            }
        }
        return new Named( name, this instanceof Named ? ((Named) this).body : this );
    }

    default NonTerminal in(char open, char close )
    {
        return in( open, this, close );
    }

    default NonTerminal inRound()
    {
        return in( '(', ')' );
    }

    default NonTerminal inCurly()
    {
        return in( '{', '}' );
    }

    default NonTerminal quoted()
    {
        return ( expr, ctx ) -> {
            char c = expr.peek();
            if ( c != '\'' && c != '"' )
            {
                expr.error( "Expected single or double quotes" );
            }
            expr.gobble();
            parse( expr, ctx );
            expr.expect( c );
        };
    }

    static NonTerminal in(char open, NonTerminal body, char close )
    {
        return ( expr, ctx ) -> {
            expr.expect( open );
            body.parse( expr, ctx );
            expr.expect( close );
        };
    }

    static NonTerminal oneOrMore(NonTerminal of )
    {
        return oneOrMore(of, ',');
    }

    static NonTerminal oneOrMore(NonTerminal of, char separator )
    {
        return (expr, ctx ) -> {
            of.parse( expr, ctx );
            // now there might be more
            expr.skipWS();
            char c = expr.peek();
            while ( c == separator )
            {
                expr.gobble(); // separator
                expr.skipWS();
                of.parse( expr, ctx );
                expr.skipWS();
                c = expr.peek();
            }
        };
    }

    static NonTerminal constant(NodeType type, String literal) {
        NonTerminal token = (expr, ctx) -> ctx.addNode(type, literal);
        return token.named(literal);
    }
}
