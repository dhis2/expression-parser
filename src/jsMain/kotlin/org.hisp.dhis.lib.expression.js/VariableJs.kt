package org.hisp.dhis.lib.expression.js

import org.hisp.dhis.lib.expression.spi.ProgramVariable

@OptIn(ExperimentalJsExport::class)
@JsExport
data class VariableJs(
    val name: ProgramVariable,
    val modifiers: QueryModifiersJs)