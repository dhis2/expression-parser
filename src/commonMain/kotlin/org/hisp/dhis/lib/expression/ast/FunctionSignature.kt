package org.hisp.dhis.lib.expression.ast

import org.hisp.dhis.lib.expression.spi.ValueType

/**
 * The signature of a [NamedFunction] as it is accepted by the grammar of a particular expression mode.
 *
 * @param function the function
 * @param parameters the parameters in the order they are accepted
 */
data class FunctionSignature(val function: NamedFunction, val parameters: List<FunctionParameter>) {

    fun getName(): String = function.getName()

    fun getReturnType(): ValueType = function.getValueType()
}

/**
 * A parameter of a [FunctionSignature].
 *
 * @param type the type expected for the argument
 * @param isOptional true, if the argument can be omitted (optional parameters are always at the end)
 * @param isVarargs true, if this is the last parameter and it can be repeated
 * @param name a descriptive name of the parameter (e.g. `regex`), or null if it is not named because the meaning is
 * obvious from the function name (e.g. the only parameter of `d2:ceil`)
 */
data class FunctionParameter(
    val type: ValueType,
    val isOptional: Boolean,
    val isVarargs: Boolean,
    val name: String? = null)
