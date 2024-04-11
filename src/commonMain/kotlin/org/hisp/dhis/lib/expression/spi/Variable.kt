package org.hisp.dhis.lib.expression.spi

data class Variable(
    val name: ProgramVariable,
    val modifiers: QueryModifiers
)
