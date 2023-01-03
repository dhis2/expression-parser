package org.hisp.dhis.expression.spi;

/**
 * Joins all required backends of the expression languages as {@link ExpressionBackend}.
 *
 * The backend connect named functions, modifiers and constants/values to their actual implementation or actual value.
 *
 * @author Jan Bernitt
 */
@FunctionalInterface
public interface ExpressionBackend extends NamedFunctionBackend, NamedValueBackend, DataItemBackend, ProgramRuleVariableBackend {
    // this interface just combines all backends
    // the reason to split them is just for better code organisation
    // that avoid large files
}
