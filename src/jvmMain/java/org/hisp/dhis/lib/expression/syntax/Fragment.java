package org.hisp.dhis.lib.expression.syntax;

import org.hisp.dhis.lib.expression.ast.NodeType;

/**
 * A {@link Fragment} is a building block of a grammar that consists of further blocks that either are {@link Fragment}s
 * themselves or are {@link Terminal}s.
 */
@FunctionalInterface
public interface Fragment {
    /**
     * Parse this non-terminal token for the expr at its current position and use the provided context to emit nodes or
     * lookup named non-terminals.
     *
     * @param expr input to process
     * @param ctx  context to build the tree and lookup named non-terminals
     */
    void parse(Expr expr, ParseContext ctx);

    /**
     * @return true, if this block is an optional argument in an argument list
     * @see #maybe()
     */
    default boolean isMaybe() {
        return false;
    }


    default boolean isVarargs() {
        return false;
    }

    /**
     * @return the name of this block as used when doing a lookup for a named block
     * @see #named(String)
     */
    default String name() {
        return null;
    }

    /**
     * This PEG parser is free of back-tracking. This means a universal "might occur" construct does not exist. The
     * (...)? block here only works in argument lists which will simply check the {@link #isMaybe()} flag. If an
     * expected argument is optional (maybe) an omitted position is not an error but valid and the end of the argument
     * list is found. This also means that optional arguments can only occur at the end of the parameter list, not in
     * the middle.
     *
     * @return This block as optional block (only in argument lists)
     */
    default Fragment maybe() {
        class Maybe extends Delegate {

            Maybe(Fragment body) {
                super(body);
            }

            @Override
            public boolean isMaybe() {
                return true;
            }
        }
        return this instanceof Maybe ? this : new Maybe(this);
    }

    default Fragment plus() {
        class Plus extends Delegate {

            Plus(Fragment body) {
                super(body);
            }

            @Override
            public boolean isVarargs() {
                return true;
            }
        }
        return this instanceof Plus ? this : new Plus(this);
    }

    default Fragment star() {
        return plus().maybe();
    }

    /**
     * By default, blocks are not named. Naming a block only serves the purpose of indexing the block as a named block
     * for later lookup.
     * <p>
     * Note that a name is a wrapper on the original block
     *
     * @return this block but with a name label attached to it
     */
    default Fragment named(String name) {
        class Named extends Delegate {
            final String name;

            Named(String name, Fragment body) {
                super(body);
                this.name = name;
            }

            @Override
            public String name() {
                return name;
            }
        }
        return new Named(name, this instanceof Named n ? n.to : this);
    }

    default Fragment quoted() {
        return (expr, ctx) -> {
            char c = expr.peek();
            boolean isQuoted = c == '\'' || c == '"';
            if (isQuoted)
                expr.gobble();
            parse(expr, ctx);
            if (isQuoted)
                expr.expect(c);
        };
    }

    static Fragment constant(NodeType type, String literal) {
        Fragment token = (expr, ctx) -> ctx.addNode(type, expr.marker(), literal);
        return token.named(literal);
    }

    abstract class Delegate implements Fragment {

        protected final Fragment to;

        protected Delegate(Fragment to) {
            this.to = to;
        }

        @Override
        public final void parse(Expr expr, ParseContext ctx) {
            to.parse(expr, ctx);
        }

        @Override
        public boolean isMaybe() {
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
