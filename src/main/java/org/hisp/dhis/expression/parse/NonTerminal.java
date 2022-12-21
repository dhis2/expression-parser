package org.hisp.dhis.expression.parse;

import org.hisp.dhis.expression.ast.NodeType;

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


    default boolean isVarargs() { return false; }

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
        class Maybe extends Delegate {

            Maybe(NonTerminal body) {
                super(body);
            }

            @Override
            public boolean isMaybe()
            {
                return true;
            }
        }
        return this instanceof Maybe ? this : new Maybe(this);
    }

    default NonTerminal plus() {
        class Plus extends Delegate {

            Plus(NonTerminal body) {
                super(body);
            }

            @Override
            public boolean isVarargs() {
                return true;
            }
        }
        return this instanceof Plus ? this : new Plus(this);
    }

    default NonTerminal star() {
        return plus().maybe();
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
        class Named extends Delegate
        {
            final String name;

            Named( String name, NonTerminal body )
            {
                super(body);
                this.name = name;
            }

            @Override
            public String name()
            {
                return name;
            }
        }
        return new Named( name, this instanceof Named ? ((Named) this).to : this );
    }

    default NonTerminal quoted()
    {
        return ( expr, ctx ) -> {
            char c = expr.peek();
            boolean isQuoted =  c == '\'' || c == '"';
            if (isQuoted)
                expr.gobble();
            parse( expr, ctx );
            if (isQuoted)
                expr.expect( c );
        };
    }

    static NonTerminal constant(NodeType type, String literal) {
        NonTerminal token = (expr, ctx) -> ctx.addNode(type, literal);
        return token.named(literal);
    }

    abstract class Delegate implements NonTerminal {

        protected final NonTerminal to;

        protected Delegate(NonTerminal to) {
            this.to = to;
        }

        @Override
        public final void parse(Expr expr, ParseContext ctx )
        {
            to.parse( expr, ctx );
        }

        @Override
        public boolean isMaybe()
        {
            return to.isMaybe();
        }

        @Override
        public boolean isVarargs() {
            return to.isVarargs();
        }

        @Override
        public String name() {
            return to.name();
        }
    }
}
