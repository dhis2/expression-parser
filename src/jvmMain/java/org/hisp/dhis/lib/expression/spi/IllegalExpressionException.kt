package org.hisp.dhis.lib.expression.spi

class IllegalExpressionException : IllegalArgumentException {
    @Transient
    private val errors: List<Issue>

    @Transient
    private val warnings: List<Issue>

    constructor(message: String) : super(message) {
        errors = listOf(Issue({ "" }, message))
        warnings = listOf()
    }

    constructor(errors: List<Issue>, warnings: List<Issue>) : super(
        String.format(
            "%d error(s), %d warning(s)",
            errors.size,
            warnings.size)) {
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
