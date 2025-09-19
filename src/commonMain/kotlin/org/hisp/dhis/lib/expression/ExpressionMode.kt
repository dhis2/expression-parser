package org.hisp.dhis.lib.expression

import org.hisp.dhis.lib.expression.eval.NodeValidator
import org.hisp.dhis.lib.expression.spi.ValueType
import org.hisp.dhis.lib.expression.syntax.ExpressionGrammar
import org.hisp.dhis.lib.expression.syntax.Fragment
import kotlin.js.JsExport

@JsExport
enum class ExpressionMode(
    internal val fragments: List<Fragment>,
    internal val validators: List<NodeValidator>,
    vararg resultTypes: ValueType) {
    // analyses data values for validity
    VALIDATION_RULE_EXPRESSION(ExpressionGrammar.ValidationRuleExpressionMode, ValueType.NUMBER),
    VALIDATION_RULE_RESULT_TEST(ExpressionGrammar.SimpleTestMode, ValueType.BOOLEAN),

    // data value generators
    PREDICTOR_GENERATOR_EXPRESSION(ExpressionGrammar.PredictorExpressionMode, ValueType.NUMBER, ValueType.STRING),

    // do a section in the data needs skipping (ignore)
    PREDICTOR_SKIP_TEST(ExpressionGrammar.PredictorSkipTestMode, ValueType.BOOLEAN),

    // ad-hoc calculated (no DB)
    // query analytics to compute some aggregate value
    INDICATOR_EXPRESSION(ExpressionGrammar.IndicatorExpressionMode, ValueType.NUMBER),

    // always SQL for entire expression
    // query analytics to compute some aggregate value
    PROGRAM_INDICATOR_EXPRESSION(ExpressionGrammar.ProgramIndicatorExpressionMode, ValueType.NUMBER),

    // never SQL (also we need JS)
    // PROGRAM_RULE_EXPRESSION
    RULE_ENGINE_CONDITION(ExpressionGrammar.RuleEngineMode, NodeValidator.RuleEngineMode, ValueType.BOOLEAN),
    RULE_ENGINE_ACTION(
        ExpressionGrammar.RuleEngineMode,
        NodeValidator.RuleEngineMode,
        ValueType.BOOLEAN,
        ValueType.STRING,
        ValueType.NUMBER,
        ValueType.DATE),

    // android custom intent request parameters
    ANDROID_CUSTOM_INTENT_EXPRESSION(ExpressionGrammar.AndroidCustomIntentMode, ValueType.NUMBER);

    internal val resultTypes: Set<ValueType> = setOf(*resultTypes)

    constructor(fragments: List<Fragment>, vararg resultTypes: ValueType) : this(
        fragments,
        listOf<NodeValidator>(),
        *resultTypes)
}