package org.hisp.dhis.lib.expression.syntax

import org.hisp.dhis.lib.expression.ast.Node
import org.hisp.dhis.lib.expression.ast.NodeType
import org.hisp.dhis.lib.expression.ast.Nodes.NamedValueNode
import org.hisp.dhis.lib.expression.ast.Nodes.TagNode
import org.hisp.dhis.lib.expression.ast.Position
import org.hisp.dhis.lib.expression.spi.ParseException
import org.hisp.dhis.lib.expression.syntax.Chars.isBinaryOperator
import org.hisp.dhis.lib.expression.syntax.Chars.isDigit
import org.hisp.dhis.lib.expression.syntax.Chars.isUnaryOperator

/**
 * An [Expr] is the fundamental building block of the expression grammar.
 *
 *
 * Aside from the actual `expr` block this class also implements the data item parsing as it is too irregular to
 * express it using composition.
 *
 * @author Jan Bernitt
 */
class Expr(
    expr: String, // whitespace recording
    private val annotate: Boolean
) {

    override fun toString(): String {
        return expr.concatToString()
    }

    fun error(desc: String?) {
        error(position(), desc)
    }

    fun error(pos0: Int, desc: String?) {
        throw ParseException(formatError(pos0, this, desc))
    }

    private val expr: CharArray
    private var pos = 0
    private var wsStart = -1
    private var wsEnd = -1
    private val wsTokens: MutableList<String> = ArrayList()

    init {
        this.expr = expr.toCharArray()
    }

    fun position(): Int {
        return pos
    }

    fun marker(): Position? {
        return if (!annotate) null else marker(0)
    }

    fun marker(offset: Int): Position? {
        if (!annotate) return null
        recordWS()
        return Position(pos + offset, wsTokens.size)
    }

    fun peek(): Char {
        return if (pos >= expr.size) Chars.EOF else expr[pos]
    }

    fun peek(ahead: Int, test: (Char) -> Boolean): Boolean {
        return pos + ahead < expr.size && test(expr[pos + ahead])
    }

    fun peek(ahead: String): Boolean {
        if (peek() != ahead[0]) {
            return false
        }
        for (i in 1 until ahead.length) {
            if (pos + i >= expr.size || expr[pos + i] != ahead[i]) {
                return false
            }
        }
        return true
    }

    fun expect(c: Char) {
        if (c != peek()) {
            error("expected $c")
        }
        gobble()
    }

    fun expect(desc: String, test: (Char) -> Boolean) {
        if (!test(peek())) {
            error("expected $desc")
        }
        gobble()
    }

    fun skipWS() {
        if (wsStart < 0) wsStart = pos
        var skipFrom = -1
        while (skipFrom < pos) {
            while (pos < expr.size && Chars.isWS(expr[pos])) pos++
            skipFrom = pos
            skipComment()
        }
        wsEnd = pos
    }

    private fun skipComment() {
        if (peek() != '/' || !peek("/*")) return
        pos += 2 // gobble(2) => /*
        var c = peek()
        while (c != Chars.EOF) {
            if (c == '*' && peek("*/")) {
                pos += 2 // gobble(2); => */
                return
            }
            pos++ // gobble; but without triggering WS tracking
            c = peek()
        }
    }

    fun skipWhile(test: (Char) -> Boolean) {
        while (peek() != Chars.EOF && test(peek())) {
            gobble()
        }
    }

    private fun recordWS() {
        if (annotate && wsStart >= 0) {
            if (wsEnd - wsStart > 0) wsTokens.add(String(expr, wsStart, wsEnd - wsStart))
            wsStart = -1
            wsEnd = -1
        }
    }

    fun gobble() {
        recordWS()
        pos++
    }

    fun gobble(n: Int) {
        recordWS()
        pos += n
    }

    fun gobbleIf(test: (Char) -> Boolean) {
        if (test(peek())) {
            gobble()
        }
    }
    /*
    Literals
    (A text pattern that is an atomic semantic unit)
     */
    /**
     * Returns the raw input between given start position and the current position
     *
     * @param start start, equal to or before current position
     * @return raw input as string
     */
    fun raw(start: Int): String {
        return String(expr, start, pos - start)
    }

    fun rawMatch(desc: String, test: (Char) -> Boolean): String {
        val s = pos
        skipWhile(test)
        if (pos == s) {
            error("expected $desc")
        }
        return raw(s)
    }

    fun rawMatch(desc: String, vararg seq: (Char) -> Boolean): String {
        val s = pos
        for (test in seq) {
            if (!test(peek())) {
                error("expected $desc")
            }
            gobble()
        }
        return raw(s)
    }

    fun rawMatch(desc: String, expected: String): String {
        for (c in expected) {
            if (peek() != c) {
                error("expected $desc")
            }
            gobble()
        }
        return expected
    }

    companion object {
        private fun formatError(pos0: Int, expr: Expr, desc: String?): String {
            var line = 1
            var posLine0 = 0
            for (p in 0 until pos0) if (expr.expr[p] == '\n') {
                line++
                posLine0 = p
            }
            val offset0 = pos0 - posLine0
            val markLen = expr.pos - pos0
            var posLineEnd = expr.pos
            while (posLineEnd < expr.expr.size && expr.expr[posLineEnd] != '\n') posLineEnd++
            val cutoutLength = posLineEnd - posLine0
            val exprCutout = String(expr.expr, posLine0, cutoutLength)
            val pointer = if (markLen <= 1) " ".repeat(offset0) + "^"
            else " ".repeat(offset0) + "^" + "-".repeat(0.coerceAtLeast(markLen - 2)) + "^"
            return "$desc\n\tat line:$line character:$offset0\n\t$exprCutout\n\t$pointer"
        }

        /**
         * The root entry point to parse an expression.
         *
         * @param expr the expression to parse
         * @param ctx  the parsing context to use to lookup fragments and build the AST
         */
        fun parse(expr: String, ctx: ParseContext, annotate: Boolean): List<String> {
            val obj = Expr(expr, annotate)
            expr(obj, ctx, true)
            obj.recordWS()
            return obj.wsTokens
        }

        /*
        Non-Terminals
        */
        fun expr(expr: Expr, ctx: ParseContext) {
            expr(expr, ctx, false)
        }

        private fun expr(expr: Expr, ctx: ParseContext, root: Boolean) {
            while (true) {
                expr1(expr, ctx)
                while (expr.peek() == '.' && expr.peek(1, Char::isLetter)) { // a dot modifier:
                    expr.gobble() // .
                    FragmentContext.lookup(expr, Literals::parseName, ctx::fragment)
                        .parse(expr, ctx)
                    expr.skipWS()
                }
                val c = expr.peek()
                if (c == 'a' && expr.peek("and") && !expr.peek(3, Chars::isIdentifier)) {
                    expr.gobble(3)
                    ctx.addNode(NodeType.BINARY_OPERATOR, expr.marker(), "and")
                }
                else if (c == 'o' && expr.peek("or") && !expr.peek(2, Chars::isIdentifier)) {
                    expr.gobble(2)
                    ctx.addNode(NodeType.BINARY_OPERATOR, expr.marker(), "or")
                }
                else if (isBinaryOperator(c)) {
                    ctx.addNode(NodeType.BINARY_OPERATOR, expr, Literals::parseBinaryOp)
                }
                else {
                    if (root && expr.pos < expr.expr.size) expr.error("Unexpected input character: '" + expr.peek() + "'")
                    return  // no more binary operators => exit loop
                }
            }
        }

        @Suppress("kotlin:S3776")
        private fun expr1(expr: Expr, ctx: ParseContext) {
            expr.skipWS()
            val c = expr.peek()
            if (isUnaryOperator(c) && expr.peek(1) { it != '=' }
                || c == 'n' && expr.peek("not") && !expr.peek(3, Chars::isIdentifier)) { // unary operators:
                expr.gobble(if (c == 'n') 3 else 1) // unary op
                ctx.addNode(NodeType.UNARY_OPERATOR, expr.marker(), if (c == 'n') "not" else c.toString())
                expr1(expr, ctx)
                return
            }
            if (c == 'd' && expr.peek("distinct") && !expr.peek(8, Chars::isIdentifier)) {
                expr.gobble(8)
                ctx.addNode(NodeType.UNARY_OPERATOR, expr.marker(), "distinct")
                expr1(expr, ctx)
                return
            }
            if (c == '(') {
                expr.gobble() // (
                ctx.beginNode(NodeType.PAR, expr.marker(-1), "")
                expr(expr, ctx)
                expr.skipWS()
                expr.expect(')')
                ctx.endNode(NodeType.PAR, expr.marker())
                expr.skipWS()
                return
            }
            if (c == '[') {
                expr.gobble()
                ctx.addNode(
                    NodeType.NAMED_VALUE,
                    Node.Factory.new(::NamedValueNode),
                    expr,
                    Literals::parseIdentifier)
                expr.expect(']')
                expr.skipWS()
                return
            }
            if (c == '\'' || c == '"') { // string literal:
                ctx.addNode(NodeType.STRING, expr, Literals::parseString)
                expr.skipWS()
                return
            }
            if (c == '.' && expr.peek(1, Char::isDigit) || isDigit(c)) { // numeric literal
                ctx.addNode(NodeType.NUMBER, expr, Literals::parseNumeric)
                expr.skipWS()
                return
            }
            // should be a named fragment then...
            FragmentContext.lookup(expr, Literals::parseName, ctx::fragment)
                .parse(expr, ctx)
            expr.skipWS()
        }

        /**
         * Entry when data items are arguments to a function
         *
         *
         * One of these:
         * <pre>
         * #{...}
         * A{...}
         * "..."
         * '...'
         * PS_EVENTDATE: UID
        </pre> *
         */
        fun dataItem(expr: Expr, ctx: ParseContext) {
            val c = expr.peek()
            if (c == '#' || c == 'A') {
                expr.gobble() // #/A
                dataItem(expr, ctx, c)
            }
            else if (c == 'V') { // V{<name>}
                expr.gobble() // V
                expr.expect('{')
                ctx.beginNode(NodeType.VARIABLE, expr.marker(-2), "V")
                ctx.addNode(NodeType.IDENTIFIER, expr, Literals::parseIdentifier)
                ctx.endNode(NodeType.VARIABLE, expr.marker(1))
                expr.expect('}')
            }
            else if (c == '"' || c == '\'') {
                // programRuleStringVariableName
                ctx.beginNode(NodeType.VARIABLE, expr.marker(), "")
                ctx.addNode(NodeType.STRING, expr, Literals::parseString)
                ctx.endNode(NodeType.VARIABLE, expr.marker())
            }
            else if (c == 'P' && expr.peek("PS_EVENTDATE:")) {
                expr.gobble(13)
                ctx.beginNode(NodeType.DATA_ITEM, expr.marker(), "#")
                ctx.beginNode(NodeType.ARGUMENT, expr.marker(), "0")
                ctx.addNode(
                    NodeType.IDENTIFIER,
                    expr.marker(),
                    "PS_EVENTDATE",
                    Node.Factory.new(::TagNode))
                expr.skipWS()
                ctx.addNode(NodeType.UID, expr, Literals::parseUid)
                ctx.endNode(NodeType.ARGUMENT, expr.marker())
                ctx.endNode(NodeType.DATA_ITEM, expr.marker())
            }
            else {
                expr.error("Incomplete or malformed value")
            }
        }

        /**
         * Direct entry point when data items are found by name in/via `expr`.
         *
         *
         * The name has already been consumed but through the method bound it can be recovered.
         */
        fun dataItemHash(expr: Expr, ctx: ParseContext) {
            dataItem(expr, ctx, '#')
        }

        /**
         * Direct entry point when data items are found by name in/via `expr`.
         *
         *
         * The name has already been consumed but through the method bound it can be recovered.
         */
        fun dataItemA(expr: Expr, ctx: ParseContext) {
            dataItem(expr, ctx, 'A')
        }

        /**
         * Indirect entry either from data items used top level or as function arguments.
         *
         *
         * At this point the name has been consumed, but it is available from the extra parameter.
         */
        private fun dataItem(expr: Expr, ctx: ParseContext, name: Char) {
            val itemStart = expr.marker(-1)
            expr.expect('{')
            var rawStart : Position? = expr.marker()
            val raw: String = expr.rawMatch("data item") { it != '}' }
            val parts = raw.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (parts.all { str: String -> isTaggedUidGroup(str) }) {
                ctx.beginNode(NodeType.DATA_ITEM, itemStart, name.toString())
                // a data item with 1-3 possibly tagged UID groups
                for (i in parts.indices) {
                    val part = parts[i]
                    val nameEndPos = part.indexOf(':')
                    ctx.beginNode(NodeType.ARGUMENT, rawStart, i.toString())
                    if (nameEndPos > 0) {
                        val tag = part.substring(0, nameEndPos)
                        ctx.addNode(
                            NodeType.IDENTIFIER,
                            rawStart,
                            tag,
                            Node.Factory.new(::TagNode))
                        rawStart = rawStart?.offsetBy(tag.length + 1) //  + :
                    }
                    for (uid in part.substring(nameEndPos + 1).split("&".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()) {
                        ctx.addNode(NodeType.UID, rawStart, uid)
                        rawStart = rawStart?.offsetBy(uid.length + 1) // + .
                    }
                    ctx.endNode(NodeType.ARGUMENT, expr.marker())
                }
                ctx.endNode(NodeType.DATA_ITEM, expr.marker(1)) // }
            }
            else if (Literals.isVarName(raw)) {
                // a programRuleVariableName
                ctx.beginNode(NodeType.VARIABLE, itemStart, name.toString())
                ctx.addNode(NodeType.IDENTIFIER, rawStart, raw)
                ctx.endNode(NodeType.VARIABLE, expr.marker(1)) // }
            }
            else {
                expr.error("Invalid value: '$raw'")
            }
            expr.expect('}')
        }

        private fun isTaggedUidGroup(expr: String): Boolean {
            val str = expr.substring(expr.indexOf(':') + 1) // strip tag
            return str.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                .all { s: String -> s == "*" || Literals.isUid(s) }
        }
    }
}
