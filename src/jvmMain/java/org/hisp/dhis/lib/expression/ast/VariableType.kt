package org.hisp.dhis.lib.expression.ast

enum class VariableType {
    PROGRAM,
    PROGRAM_RULE;

    companion object {
        @JvmStatic
        fun fromSymbol(symbol: String): VariableType {
            return if ("V" == symbol) PROGRAM else PROGRAM_RULE
        }
    }
}
