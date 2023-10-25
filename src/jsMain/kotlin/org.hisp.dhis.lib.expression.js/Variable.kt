package org.hisp.dhis.lib.expression.js

import org.hisp.dhis.lib.expression.ast.ProgramVariable

@OptIn(ExperimentalJsExport::class)
@JsExport
data class Variable(
    val name: String,
    val modifiers: QueryModifiers) {

    init {
        require(NAMES.contains(name)) { "Variable name must be one of: $NAMES" }
    }

    companion object {
        val NAMES = ProgramVariable.entries.map { it.name }.toTypedArray()
    }
}