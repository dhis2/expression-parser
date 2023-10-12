package org.hisp.dhis.lib.expression.spi

import org.hisp.dhis.lib.expression.ast.Node
import org.hisp.dhis.lib.expression.eval.Evaluate

data class Issues(
    val errors: MutableList<Issue>,
    val warnings: MutableList<Issue>
) {
    constructor() : this(arrayListOf(), arrayListOf())

    fun throwIfErrorsOrWarnings() {
        if (errors.isNotEmpty() || warnings.isNotEmpty()) {
            throw IllegalExpressionException(errors, warnings)
        }
    }

    fun addError(node: Node<*>, message: String) {
        errors.add(Issue(position(node), message))
    }

    fun addWarning(node: Node<*>, message: String) {
        warnings.add(Issue(position(node), message))
    }

    fun addIssue(warning: Boolean, node: Node<*>, message: String) {
        if (warning) {
            addWarning(node, message)
        }
        else {
            addError(node, message)
        }
    }

    companion object {
        private fun position(node: Node<*>): () -> String {
            return { Evaluate.normalise(node) }
        }
    }
}
