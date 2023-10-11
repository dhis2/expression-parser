package org.hisp.dhis.lib.expression.spi

import org.hisp.dhis.lib.expression.ast.ProgramVariable

data class Variable(
    val name: ProgramVariable,
    val modifiers: QueryModifiers
)
