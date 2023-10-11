package org.hisp.dhis.lib.expression.syntax

/**
 * Named character sets.
 */
object Chars {

    fun isVarName(c: Int): Boolean {
        return isVarName(c.toChar())
    }

    fun isVarName(c: Char): Boolean {
        return isIdentifier(c) || isDigit(c) || c == '-' || c == '.' || isWS(c)
    }

    fun isName(c: Char): Boolean {
        return c == '#' || c == ':' || c == '_' || c == '.' || isAlphaNumeric(c)
    }

    fun isIdentifier(c: Char): Boolean {
        return c == '_' || isLetter(c)
    }

    fun isBinaryOperator(c: Char): Boolean {
        return isArithmeticOperator(c) || isLogicOperator(c) || isComparisonOperator(c)
    }

    fun isUnaryOperator(c: Char): Boolean {
        return isSignOperator(c) || c == '!'
    }

    fun isArithmeticOperator(c: Char): Boolean {
        return isSignOperator(c) || c == '*' || c == '/' || c == '%' || c == '^'
    }

    fun isSignOperator(c: Char): Boolean {
        return c == '+' || c == '-'
    }

    fun isLogicOperator(c: Char): Boolean {
        return c == '&' || c == '|'
    }

    fun isComparisonOperator(c: Char): Boolean {
        return c == '<' || c == '>' || c == '=' || c == '!'
    }

    fun isWS(c: Char): Boolean {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r'
    }

    fun isLetter(c: Char): Boolean {
        return c in 'a'..'z' || c in 'A'..'Z'
    }

    fun isAlphaNumeric(c: Int): Boolean {
        return isAlphaNumeric(c.toChar())
    }

    fun isAlphaNumeric(c: Char): Boolean {
        return isLetter(c) || isDigit(c)
    }

    fun isDigit(c: Char): Boolean {
        return c in '0'..'9'
    }

    fun isHexDigit(c: Char): Boolean {
        return c in 'a'..'f' || c in 'A'..'F' || isDigit(c)
    }

    fun isOctalDigit(c: Char): Boolean {
        return c in '0'..'7'
    }

    const val EOF = 0.toChar()
}
