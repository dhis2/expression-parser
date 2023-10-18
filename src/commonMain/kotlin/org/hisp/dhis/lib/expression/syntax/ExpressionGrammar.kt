package org.hisp.dhis.lib.expression.syntax

import org.hisp.dhis.lib.expression.ast.*
import org.hisp.dhis.lib.expression.ast.Nodes.AggregationTypeNode
import org.hisp.dhis.lib.expression.ast.Nodes.ProgramVariableNode
import org.hisp.dhis.lib.expression.ast.Nodes.ReportingRateTypeNode
import org.hisp.dhis.lib.expression.spi.DataItemType
import org.hisp.dhis.lib.expression.syntax.Fragment.Companion.constant

/**
 * Declaration of the DHIS2 expression language.
 *
 *
 * The language is composed out of [Terminal]s and named [Fragment]s.
 *
 * @author Jan Bernitt
 */
object ExpressionGrammar {

    /*
    Terminals (simple building blocks)
    */
    private val STRING: Terminal = Terminal{ NodeType.STRING }
    private val INTEGER: Terminal = Terminal { NodeType.INTEGER }
    private val UID: Terminal = Terminal { NodeType.UID }
    private val IDENTIFIER: Terminal = Terminal { NodeType.IDENTIFIER }
    private val DATE: Terminal = Terminal { NodeType.DATE }

    /*
    Essential composed building blocks
    */
    private val expr: Fragment = Fragment {expr, ctx ->  Expr.expr(expr, ctx)}
    private val dataItem: Fragment = Fragment {expr, ctx -> Expr.dataItem(expr, ctx)}
    private val dataItemHash: Fragment = Fragment {expr, ctx -> Expr.dataItemHash(expr, ctx)}
    private val dataItemA: Fragment = Fragment {expr, ctx -> Expr.dataItemA(expr, ctx)}

    /*
    Production Rules
    */
    private val STAGE_OFFSET = mod(DataItemModifier.stageOffset, INTEGER)
    private val MAX_DATE = mod(DataItemModifier.maxDate, DATE)
    private val MIN_DATE = mod(DataItemModifier.minDate, DATE)
    private val AGGREGATION_TYPE = mod(DataItemModifier.aggregationType, IDENTIFIER.by(Node.Factory.new(::AggregationTypeNode)))
    private val PERIOD_OFFSET = mod(DataItemModifier.periodOffset, INTEGER)
    private val YEAR_TO_DATE = mod(DataItemModifier.yearToDate)
    private val SUB_EXPRESSION = fn(NamedFunction.subExpression, expr)

    private val CommonFunctions = listOf( // (alphabetical)
        fn(NamedFunction.firstNonNull, expr.plus()),
        fn(NamedFunction.greatest, expr.plus()),
        fn(NamedFunction.ifThenElse, expr, expr, expr),
        fn(NamedFunction.isNotNull, expr),
        fn(NamedFunction.isNull, expr),
        fn(NamedFunction.least, expr.plus()),
        fn(NamedFunction.log, expr, expr.maybe()),
        fn(NamedFunction.log10, expr),
        fn(NamedFunction.removeZeros, expr)
    )
    private val ValidationRuleFunctions = listOf(
        fn(NamedFunction.orgUnit_ancestor, UID.plus()),
        fn(NamedFunction.orgUnit_dataSet, UID.plus()),
        fn(NamedFunction.orgUnit_group, UID.plus()),
        fn(NamedFunction.orgUnit_program, UID.plus())
    )
    private val CommonAggregationFunctions = listOf(
        fn(NamedFunction.avg, expr),
        fn(NamedFunction.count, expr),
        fn(NamedFunction.max, expr),
        fn(NamedFunction.min, expr),
        fn(NamedFunction.stddev, expr),
        fn(NamedFunction.sum, expr)
    )
    private val PredictorAggregationFunctions = listOf( // (alphabetical)
        fn(NamedFunction.median, expr),
        fn(NamedFunction.percentileCont, expr, expr),
        fn(NamedFunction.stddevPop, expr),
        fn(NamedFunction.stddevSamp, expr),
        fn(NamedFunction.variance, expr),
        fn(NamedFunction.normDistCum, expr, expr.maybe(), expr.maybe()),
        fn(NamedFunction.normDistDen, expr, expr.maybe(), expr.maybe()),
    )
    private val CommonD2Functions = listOf( // (alphabetical)
        fn(NamedFunction.d2_count, dataItem),
        fn(NamedFunction.d2_countIfValue, dataItem, expr),
        fn(NamedFunction.d2_daysBetween, expr, expr),
        fn(NamedFunction.d2_hasValue, dataItem),
        fn(NamedFunction.d2_maxValue, dataItem),
        fn(NamedFunction.d2_minValue, dataItem),
        fn(NamedFunction.d2_monthsBetween, expr, expr),
        fn(NamedFunction.d2_oizp, expr),
        fn(NamedFunction.d2_weeksBetween, expr, expr),
        fn(NamedFunction.d2_yearsBetween, expr, expr),
        fn(NamedFunction.d2_zing, expr),
        fn(NamedFunction.d2_zpvc, expr.plus())
    )
    private val RuleEngineD2Functions = listOf(
        fn(NamedFunction.d2_addDays, expr, expr),
        fn(NamedFunction.d2_ceil, expr),
        fn(NamedFunction.d2_concatenate, expr.plus()),
        fn(NamedFunction.d2_countIfZeroPos, dataItem),
        fn(NamedFunction.d2_extractDataMatrixValue, expr, expr),
        fn(NamedFunction.d2_floor, expr),
        fn(NamedFunction.d2_hasUserRole, expr),
        fn(NamedFunction.d2_inOrgUnitGroup, expr),
        fn(NamedFunction.d2_lastEventDate, expr),
        fn(NamedFunction.d2_left, expr, expr),
        fn(NamedFunction.d2_length, expr),
        fn(NamedFunction.d2_modulus, expr, expr),
        fn(NamedFunction.d2_right, expr, expr),
        fn(NamedFunction.d2_round, expr, INTEGER.maybe()),
        fn(NamedFunction.d2_split, expr, expr, expr),
        fn(NamedFunction.d2_substring, expr, expr, expr),
        fn(NamedFunction.d2_validatePattern, expr, expr),
        fn(NamedFunction.d2_zScoreHFA, expr, expr, expr),
        fn(NamedFunction.d2_zScoreWFA, expr, expr, expr),
        fn(NamedFunction.d2_zScoreWFH, expr, expr, expr)
    )
    private val ProgramIndicatorD2Functions = listOf(
        fn(NamedFunction.d2_condition, STRING, expr, expr),
        fn(NamedFunction.d2_countIfCondition, expr, STRING),
        fn(NamedFunction.d2_minutesBetween, expr, expr),
        fn(NamedFunction.d2_relationshipCount, UID.quoted().maybe())
    )
    private val CommonConstants = listOf(
        constant(NodeType.NULL, "null"),
        constant(NodeType.BOOLEAN, "true"),
        constant(NodeType.BOOLEAN, "false")
    )
    private val HASH_BRACE = dataItemHash.named(DataItemType.DATA_ELEMENT.symbol)
    private val A_BRACE = dataItemA.named(DataItemType.ATTRIBUTE.symbol)
    private val C_BRACE = item(DataItemType.CONSTANT, UID)
    private val D_BRACE = item(DataItemType.PROGRAM_DATA_ELEMENT, UID, UID)
    private val I_BRACE = item(DataItemType.PROGRAM_INDICATOR, UID)
    private val R_BRACE = item(DataItemType.REPORTING_RATE, UID, IDENTIFIER.by(Node.Factory.new(::ReportingRateTypeNode)))
    private val OUG_BRACE = item(DataItemType.ORG_UNIT_GROUP, UID)
    private val N_BRACE = item(DataItemType.INDICATOR, UID)
    private val V_BRACE = variable(DataItemType.PROGRAM_VARIABLE, IDENTIFIER.by(Node.Factory.new(::ProgramVariableNode)))
    private val CommonDataItems = listOf(HASH_BRACE, A_BRACE, C_BRACE, D_BRACE, I_BRACE, R_BRACE, OUG_BRACE)

    /*
    Modes
    */
    val ValidationRuleExpressionMode = concat(
        CommonFunctions,
        CommonDataItems,
        CommonConstants,
        ValidationRuleFunctions)

    val PredictorExpressionMode = concat(
        ValidationRuleExpressionMode,
        CommonAggregationFunctions,
        PredictorAggregationFunctions,
        listOf(MIN_DATE, MAX_DATE))

    val IndicatorExpressionMode = concat(
        CommonFunctions,
        CommonDataItems,
        CommonConstants,
        listOf(N_BRACE, SUB_EXPRESSION, AGGREGATION_TYPE, MIN_DATE, MAX_DATE, PERIOD_OFFSET, YEAR_TO_DATE))

    val PredictorSkipTestMode = PredictorExpressionMode

    val SimpleTestMode = concat(CommonFunctions, CommonConstants, listOf(C_BRACE))

    val ProgramIndicatorExpressionMode = concat(
        CommonFunctions,
        CommonConstants,
        CommonD2Functions,
        CommonAggregationFunctions,
        ProgramIndicatorD2Functions,
        listOf(HASH_BRACE, A_BRACE, C_BRACE, V_BRACE, STAGE_OFFSET))

    val RuleEngineMode = concat(
        CommonConstants,
        CommonD2Functions,
        RuleEngineD2Functions,
        listOf(HASH_BRACE, A_BRACE, C_BRACE, V_BRACE))

    /*
    Block expressions
    */
    private fun mod(modifier: DataItemModifier, vararg args: Fragment): Fragment {
        val name = modifier.name
        return block(NodeType.MODIFIER, name, '(', ',', ')', *args).named(name)
    }

    private fun fn(function: NamedFunction, vararg args: Fragment): Fragment {
        val name = function.getName()
        return block(NodeType.FUNCTION, name, '(', ',', ')', *args).named(name)
    }

    fun item(value: DataItemType, vararg args: Fragment): Fragment {
        val symbol = value.symbol
        return block(NodeType.DATA_ITEM, symbol, '{', '.', '}', *args).named(symbol)
    }

    fun variable(value: DataItemType, vararg args: Fragment): Fragment {
        val symbol = value.symbol
        return block(NodeType.VARIABLE, symbol, '{', '.', '}', *args).named(symbol)
    }

    @Suppress("kotlin:S3776")
    private fun block(
        type: NodeType,
        name: String?,
        start: Char,
        argsSeparator: Char,
        end: Char,
        vararg args: Fragment
    ): Fragment {
        return Fragment { expr: Expr, ctx: ParseContext ->
            expr.expect(start)
            val sPos = expr.marker(-1)
            expr.skipWS()
            ctx.beginNode(type, sPos, name!!)
            var i = 0
            while (i < args.size || args.isNotEmpty() && args[args.size - 1].isVarargs()) {
                expr.skipWS()
                val arg = args[i.coerceAtMost(args.size - 1)]
                val c = expr.peek()
                if (c == end) {
                    if (arg.isMaybe() || args[args.size - 1].isVarargs()) {
                        expr.expect(end)
                        ctx.endNode(type, expr.marker())
                        return@Fragment
                    }
                    expr.error("Expected more arguments: "
                            + args
                            .drop(i)
                            .map { a: Fragment -> if (a.name() == null) "?" else a.name() }
                            .joinToString(","))
                }
                if (i > 0) {
                    if (c != argsSeparator) expr.error("Expected $argsSeparator or $end")
                    expr.gobble() // separator
                    expr.skipWS()
                }
                val wrapInArgument = type !== NodeType.VARIABLE
                if (wrapInArgument) ctx.beginNode(NodeType.ARGUMENT, expr.marker(), i.toString())
                arg.parse(expr, ctx)
                if (wrapInArgument) ctx.endNode(NodeType.ARGUMENT, expr.marker())
                i++
            }
            expr.skipWS()
            expr.expect(end)
            ctx.endNode(type, expr.marker())
        }
    }

    private fun concat(vararg nonTerminals: List<Fragment>): List<Fragment> {
        return nonTerminals.flatMap { it }
    }
}
