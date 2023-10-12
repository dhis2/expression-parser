package org.hisp.dhis.lib.expression.spi

class IllegalExpressionException : IllegalArgumentException {

    private val errors: List<Issue>
    private val warnings: List<Issue>

    constructor(message: String) : super(message) {
        errors = listOf(Issue({ "" }, message))
        warnings = listOf()
    }

    constructor(errors: List<Issue>, warnings: List<Issue>) : super(
        "${errors.size} error(s), ${warnings.size} warning(s)") {
        this.errors = errors
        this.warnings = warnings
    }

    fun getErrors() : List<Issue> {
        return errors;
    }

    fun getWarnings() : List<Issue> {
        return warnings;
    }
}
