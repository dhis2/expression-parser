package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.spi.ExpressionData
import org.hisp.dhis.lib.expression.spi.VariableValue

abstract class VariableTest {

    fun evaluate(expression: String, values: Map<String, VariableValue>) : Any? {
        val data: ExpressionData = ExpressionData().copy(programRuleVariableValues = values)
        return Expression(expression, Expression.Mode.RULE_ENGINE_ACTION).evaluate( { _: String? -> null }, data)
    }
}