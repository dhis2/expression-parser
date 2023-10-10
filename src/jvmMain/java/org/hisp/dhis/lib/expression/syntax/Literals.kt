package org.hisp.dhis.lib.expression.syntax

import org.hisp.dhis.lib.expression.ast.NodeType
import org.hisp.dhis.lib.expression.syntax.Chars.isArithmeticOperator
import org.hisp.dhis.lib.expression.syntax.Chars.isComparisonOperator
import org.hisp.dhis.lib.expression.syntax.Chars.isDigit
import org.hisp.dhis.lib.expression.syntax.Chars.isLetter
import org.hisp.dhis.lib.expression.syntax.Chars.isLogicOperator
import org.hisp.dhis.lib.expression.syntax.Chars.isUnaryOperator
import java.util.stream.IntStream

object Literals {

    fun parse(expr: Expr, type: NodeType): String {
        return when (type) {
            NodeType.BINARY_OPERATOR -> parseBinaryOp(expr)
            NodeType.UNARY_OPERATOR -> parseUnaryOp(expr)
            NodeType.NUMBER -> parseNumeric(expr)
            NodeType.INTEGER -> parseInteger(expr)
            NodeType.STRING -> parseString(expr)
            NodeType.BOOLEAN -> parseBoolean(expr)
            NodeType.DATE -> parseDate(expr)
            NodeType.UID -> parseUid(expr)
            NodeType.NAMED_VALUE, NodeType.IDENTIFIER -> parseIdentifier(expr)
            else -> {
                expr.error("Not a literal type: $type")
                null!!
            }
        }
    }

    fun parseUnaryOp(expr: Expr): String {
        val c = expr.peek()
        if (isUnaryOperator(c)) {
            expr.error("unary operator")
        }
        return c.toString()
    }

    fun parseIdentifier(expr: Expr): String {
        return expr.rawMatch("identifier", Chars::isIdentifier)
    }

    fun parseString(expr: Expr): String {
        val cq = expr.peek()
        if (cq != '"' && cq != '\'') {
            expr.error("expected start of string literal")
        }
        expr.gobble()
        val s = expr.position()
        var c = expr.peek()
        while (c != Chars.EOF) {
            if (c == '\\') {
                expr.gobble() // the \
                c = expr.peek()
                if (c == 'u') { // uXXXX:
                    expr.gobble() // the u
                    expr.rawMatch("hex", Chars::isHexDigit, Chars::isHexDigit, Chars::isHexDigit, Chars::isHexDigit)

                }
                else if (isDigit(c)) { // 888:
                    expr.rawMatch("octal", Chars::isOctalDigit, Chars::isOctalDigit, Chars::isOctalDigit)
                }
                else { // escape code
                    expr.gobble() // the escape code
                }
            }
            else if (c == '\n' || c == '\r' || c == cq) {
                val str = expr.raw(s)
                expr.gobble() // line break or closing quotes
                return str
            }
            else {
                expr.gobble() // the plain character
            }
            c = expr.peek()
        }
        expr.error("unclosed string literal, expected closing $cq")
        return null!!
    }

    fun parseName(expr: Expr): String {
        return expr.rawMatch("name", Chars::isName)
    }

    fun parseInteger(expr: Expr): String {
        val s = expr.position()
        expr.gobbleIf(Chars::isSignOperator)
        expr.skipWhile(Chars::isDigit)
        return expr.raw(s)
    }

    fun parseNumeric(expr: Expr): String {
        val s = expr.position()
        expr.gobbleIf(Chars::isSignOperator)
        val hasInt = isDigit(expr.peek())
        if (hasInt) {
            expr.skipWhile(Chars::isDigit)
        }
        if (!hasInt || expr.peek() == '.') {
            expr.expect('.')
            expr.skipWhile(Chars::isDigit)
        }
        val c = expr.peek()
        if (c == 'e' || c == 'E') {
            expr.gobble() // e/E
            expr.gobbleIf(Chars::isSignOperator)
            expr.skipWhile(Chars::isDigit)
        }
        return expr.raw(s)
    }

    fun parseBoolean(expr: Expr): String {
        return expr.rawMatch("boolean", if (expr.peek() == 't') "true" else "false")
    }

    fun parseDate(expr: Expr): String {
        // [1-9] [0-9] [0-9] [0-9] '-' [0-1]? [0-9] '-' [0-3]? [0-9]
        val s = expr.position()
        expr.expect("digit", Chars::isDigit)
        expr.expect("digit", Chars::isDigit)
        expr.expect("digit", Chars::isDigit)
        expr.expect("digit", Chars::isDigit)
        expr.expect('-')
        expr.expect("digit", Chars::isDigit)
        if (expr.peek() != '-') {
            expr.expect("digit", Chars::isDigit)
        }
        expr.expect('-')
        expr.expect("digit", Chars::isDigit)
        expr.gobbleIf(Chars::isDigit)
        return expr.raw(s)
    }

    fun parseUid(expr: Expr): String {
        val c = expr.peek()
        if (c == '*') {
            expr.expect('*')
            return "*"
        }
        return expr.rawMatch(
            "uid", Chars::isLetter,
            Chars::isAlphaNumeric, Chars::isAlphaNumeric, Chars::isAlphaNumeric, Chars::isAlphaNumeric, Chars::isAlphaNumeric,
            Chars::isAlphaNumeric, Chars::isAlphaNumeric, Chars::isAlphaNumeric, Chars::isAlphaNumeric, Chars::isAlphaNumeric)
    }

    fun parseBinaryOp(expr: Expr): String {
        val c = expr.peek()
        val s = expr.position()
        if (isArithmeticOperator(c)) { // + - * / % ^
            expr.gobble()
            return expr.raw(s)
        }
        if (isLogicOperator(c)) { // && ||
            expr.gobble()
            expr.gobbleIf(Chars::isLogicOperator )
            return expr.raw(s)
        }
        else if (isComparisonOperator(c)) { // > < >= <= == != <>
            expr.gobble()
            expr.gobbleIf(Chars::isComparisonOperator)
            return expr.raw(s)
        }
        expr.error("expected operator")
        return null!!
    }

    fun isUid(s: String): Boolean {
        return s.length == 11 && isLetter(s[0]) && chars(s).allMatch(Chars::isAlphaNumeric)
    }

    fun isVarName(s: String): Boolean {
        return s.isNotEmpty() && chars(s).allMatch(Chars::isVarName)
    }

    fun chars(s: String): IntStream {
        val intList: MutableList<Int> = ArrayList()
        for (i in 0 until s.length) {
            intList.add(s[i].code)
        }
        return intList.stream().mapToInt { obj: Int -> obj }
    }
}
