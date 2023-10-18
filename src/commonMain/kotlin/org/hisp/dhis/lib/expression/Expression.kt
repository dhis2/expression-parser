package org.hisp.dhis.lib.expression

import org.hisp.dhis.lib.expression.ast.Node
import org.hisp.dhis.lib.expression.ast.VariableType
import org.hisp.dhis.lib.expression.eval.Api.collectDataItems
import org.hisp.dhis.lib.expression.eval.Api.collectUIDs
import org.hisp.dhis.lib.expression.eval.Api.collectVariableNames
import org.hisp.dhis.lib.expression.eval.Api.collectVariables
import org.hisp.dhis.lib.expression.eval.Api.describe
import org.hisp.dhis.lib.expression.eval.Api.evaluate
import org.hisp.dhis.lib.expression.eval.Api.normalise
import org.hisp.dhis.lib.expression.eval.Api.regenerate
import org.hisp.dhis.lib.expression.eval.Api.validate
import org.hisp.dhis.lib.expression.eval.NodeValidator
import org.hisp.dhis.lib.expression.spi.*
import org.hisp.dhis.lib.expression.syntax.ExpressionGrammar
import org.hisp.dhis.lib.expression.syntax.Fragment
import org.hisp.dhis.lib.expression.syntax.Parser

/**
 * Facade API for working with DHIS2 expressions.
 *
 * @author Jan Bernitt
 */
class Expression(
    private val expression: String,
    private val mode: Mode = Mode.PREDICTOR_GENERATOR_EXPRESSION,
    annotate: Boolean = false
) {
    enum class Mode(val fragments: List<Fragment>, val validators: List<NodeValidator>, vararg resultTypes: ValueType) {
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
            ValueType.DATE);

        val resultTypes: Set<ValueType>

        constructor(fragments: List<Fragment>, vararg resultTypes: ValueType) : this(
            fragments,
            listOf<NodeValidator>(),
            *resultTypes)

        init {
            this.resultTypes = setOf(*resultTypes)
        }
    }

    private val root: Node<*>

    init {
        this.root = Parser.parse(expression, mode.fragments, annotate)
    }

    fun collectDataItems(): Set<DataItem> {
        return collectDataItems(root)
    }

    fun collectProgramRuleVariableNames(): Set<String> {
        return collectVariableNames(root, VariableType.PROGRAM_RULE)
    }

    fun collectProgramVariablesNames(): Set<String> {
        return collectVariableNames(root, VariableType.PROGRAM)
    }

    /**
     * For testing only.
     *
     * @see .evaluate
     */
    fun evaluate(): Any? {
        return evaluate({ _: String? -> null }, ExpressionData())
    }

    fun evaluate(functions: ExpressionFunctions, data: ExpressionData): Any? {
        return evaluate(root, functions, data)
    }

    fun collectProgramVariables(): Set<Variable> {
        return collectVariables(root, VariableType.PROGRAM)
    }

    fun generateSQL(functions: ExpressionFunctions, sqlByProgramVariable: Map<Variable, String>): String {
        return "" //TODO
    }

    /**
     * Collects all ID that are UID values.
     *
     *
     * OBS! This does not include [ID]s that are not [ID.Type.isUID].
     *
     * @return A set of [ID]s used in the expression.
     */
    fun collectUIDs(): Set<ID> {
        return collectUIDs(root)
    }

    fun describe(displayNames: Map<String, String>): String {
        return describe(root, displayNames)
    }

    fun validate(displayNamesKeys: Map<String, ValueType>) {
        val programRuleVariableValues: MutableMap<String, VariableValue> = HashMap()
        displayNamesKeys.forEach { (key: String, value: ValueType) ->
            programRuleVariableValues[key] = VariableValue(value)
        }
        val data: ExpressionData = ExpressionData()
            .copy(programRuleVariableValues = programRuleVariableValues)
        validate(root, data, mode.validators, mode.resultTypes)
    }

    fun collectDataItemForRegenerate(): Set<DataItem> {
        return collectDataItems(root, DataItemType.CONSTANT, DataItemType.ORG_UNIT_GROUP)
    }

    /**
     * Regenerates an expression from the parse tree where all constant IDs are substituted with their values and all
     * organisation unit groups IDs are substituted with their member count provided that are contained in the given
     * map.
     *
     * @param dataItemValues values for constants, member count for organisation unit groups
     * @return an expression where constant and organisation unit group data items are substituted with values
     * @see .collectDataItemForRegenerate
     */
    fun regenerate(dataItemValues: Map<DataItem, Number>): String {
        // old: org.hisp.dhis.expression.DefaultExpressionService#regenerateIndicatorExpression (indicator only)
        return regenerate(root, dataItemValues)
    }

    /**
     * @return the expression in its normalised from (str => AST => str)
     */
    fun normalise(): String {
        return normalise(root)
    }

    /**
     * @return the expression in its original (user input) form
     */
    override fun toString(): String {
        return expression
    }
}
