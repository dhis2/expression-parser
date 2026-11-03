package org.hisp.dhis.lib.expression.syntax

import org.hisp.dhis.lib.expression.ast.FunctionParameter
import org.hisp.dhis.lib.expression.ast.FunctionSignature
import org.hisp.dhis.lib.expression.ast.NamedFunction

/**
 * A [Fragment] for a [NamedFunction] call that keeps track of the function and its argument [Fragment]s, so the
 * [FunctionSignature] can be derived from the grammar itself.
 */
internal class FunctionFragment(
    val function: NamedFunction,
    private val args: List<Fragment>,
    body: Fragment
) : Fragment.Delegate(body) {

    init {
        val types = function.parameterTypes
        require(types.size == args.size) {
            "${function.getName()} declares ${types.size} parameter types but the grammar has ${args.size} arguments"
        }
        require(function.isVarargs == (args.lastOrNull()?.isVarargs() == true)) {
            "${function.getName()} varargs declaration does not match the grammar"
        }
    }

    override fun name(): String {
        return function.getName()
    }

    fun signature(): FunctionSignature {
        val types = function.parameterTypes
        return FunctionSignature(function, args.mapIndexed { i, arg ->
            FunctionParameter(arg.literalType() ?: types[i], arg.isMaybe(), arg.isVarargs(), arg.paramName())
        })
    }

    companion object {

        /**
         * @return the [FunctionFragment] the given fragment is or wraps, or null if it is not a function fragment
         */
        fun of(fragment: Fragment): FunctionFragment? {
            return generateSequence(fragment) { (it as? Fragment.Delegate)?.to }
                .filterIsInstance<FunctionFragment>()
                .firstOrNull()
        }
    }
}
