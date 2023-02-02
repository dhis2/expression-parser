package org.hisp.dhis.expression.syntax;

import org.hisp.dhis.expression.ast.DataItemModifier;
import org.hisp.dhis.expression.ast.NamedFunction;
import org.hisp.dhis.expression.ast.NodeType;
import org.hisp.dhis.expression.ast.Nodes;
import org.hisp.dhis.expression.ast.VariableType;
import org.hisp.dhis.expression.spi.DataItemType;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableList;

/**
 * Declaration of the DHIS2 expression language.
 *
 * The language is composed out of {@link Terminal}s and named {@link Fragment}s.
 *
 * @author Jan Bernitt
 */
@SuppressWarnings("java:S2386")
public interface ExpressionGrammar
{
    /*
    Terminals (simple building blocks)
     */

    Terminal
            STRING     = () -> NodeType.STRING,
            INTEGER    = () -> NodeType.INTEGER,
            UID        = () -> NodeType.UID,
            IDENTIFIER = () -> NodeType.IDENTIFIER,
            DATE       = () -> NodeType.DATE;

    /*
    Essential composed building blocks
     */

    Fragment
            expr = Expr::expr,
            dataItem = Expr::dataItem,
            dataItemHash = Expr::dataItemHash,
            dataItemA = Expr::dataItemA;

    /*
    Production Rules
     */

    Fragment STAGE_OFFSET = mod( DataItemModifier.stageOffset, INTEGER);
    Fragment MAX_DATE = mod( DataItemModifier.maxDate, DATE);
    Fragment MIN_DATE = mod( DataItemModifier.minDate, DATE);
    Fragment AGGREGATION_TYPE = mod( DataItemModifier.aggregationType, IDENTIFIER.as(Nodes.AggregationTypeNode::new) );
    Fragment PERIOD_OFFSET = mod( DataItemModifier.periodOffset, INTEGER);
    Fragment YEAR_TO_DATE = mod( DataItemModifier.yearToDate);

    Fragment SUB_EXPRESSION = fn( NamedFunction.subExpression , expr );

    List<Fragment> CommonFunctions = List.of( // (alphabetical)
            fn( NamedFunction.firstNonNull , expr.plus() ),
            fn( NamedFunction.greatest , expr.plus() ),
            fn( NamedFunction.ifThenElse , expr, expr, expr ),
            fn( NamedFunction.isNotNull , expr ),
            fn( NamedFunction.isNull , expr ),
            fn( NamedFunction.least , expr.plus() ),
            fn( NamedFunction.log , expr, expr.maybe() ),
            fn( NamedFunction.log10 , expr ),
            fn( NamedFunction.removeZeros , expr )
    );

    List<Fragment> ValidationRuleFunctions = List.of(
            fn( NamedFunction.orgUnit_ancestor , UID.plus() ),
            fn( NamedFunction.orgUnit_dataSet , UID.plus() ),
            fn( NamedFunction.orgUnit_group , UID.plus() ),
            fn( NamedFunction.orgUnit_program , UID.plus() )
    );

    List<Fragment> CommonAggregationFunctions = List.of(
            fn( NamedFunction.avg, expr ),
            fn( NamedFunction.count, expr),
            fn( NamedFunction.max , expr ),
            fn( NamedFunction.min , expr ),
            fn( NamedFunction.stddev , expr ),
            fn( NamedFunction.sum , expr )
    );

    List<Fragment> PredictorAggregationFunctions = List.of( // (alphabetical)
            fn( NamedFunction.median , expr ),
            fn( NamedFunction.percentileCont , expr, expr ),
            fn( NamedFunction.stddevPop , expr ),
            fn( NamedFunction.stddevSamp , expr ),
            fn( NamedFunction.variance , expr )
    );

    List<Fragment> CommonD2Functions = List.of( // (alphabetical)
            fn( NamedFunction.d2_count , dataItem),
            fn( NamedFunction.d2_countIfValue , dataItem, expr ),
            fn( NamedFunction.d2_daysBetween , expr, expr ),
            fn( NamedFunction.d2_hasValue , dataItem),
            fn( NamedFunction.d2_maxValue , dataItem),
            fn( NamedFunction.d2_minValue , dataItem),
            fn( NamedFunction.d2_monthsBetween , expr, expr ),
            fn( NamedFunction.d2_oizp , expr ),
            fn( NamedFunction.d2_weeksBetween , expr, expr ),
            fn( NamedFunction.d2_yearsBetween , expr, expr ),
            fn( NamedFunction.d2_zing , expr ),
            fn( NamedFunction.d2_zpvc , expr.plus() )
    );

    List<Fragment> RuleEngineD2Functions = List.of(
            fn( NamedFunction.d2_addDays , expr, expr ),
            fn( NamedFunction.d2_ceil , expr ),
            fn( NamedFunction.d2_concatenate , expr.plus() ),
            fn( NamedFunction.d2_countIfZeroPos , dataItem),
            fn( NamedFunction.d2_extractDataMatrixValue , expr, expr ),
            fn( NamedFunction.d2_floor, expr ),
            fn( NamedFunction.d2_hasUserRole , expr ),
            fn( NamedFunction.d2_inOrgUnitGroup , expr ),
            fn( NamedFunction.d2_lastEventDate , expr ),
            fn( NamedFunction.d2_left , expr, expr ),
            fn( NamedFunction.d2_length , expr ),
            fn( NamedFunction.d2_modulus , expr, expr ),
            fn( NamedFunction.d2_right , expr, expr ),
            fn( NamedFunction.d2_round , expr, INTEGER.maybe() ),
            fn( NamedFunction.d2_split , expr, expr, expr ),
            fn( NamedFunction.d2_substring , expr, expr, expr ),
            fn( NamedFunction.d2_validatePattern , expr, expr ),
            fn( NamedFunction.d2_zScoreHFA , expr, expr, expr ),
            fn( NamedFunction.d2_zScoreWFA , expr, expr, expr ),
            fn( NamedFunction.d2_zScoreWFH , expr, expr, expr )
    );

    List<Fragment> ProgramIndicatorD2Functions = List.of(
            fn( NamedFunction.d2_condition , STRING, expr, expr ),
            fn( NamedFunction.d2_countIfCondition , expr, STRING),
            fn( NamedFunction.d2_minutesBetween , expr, expr ),
            fn( NamedFunction.d2_relationshipCount , UID.quoted().maybe() )
    );

    List<Fragment> CommonConstants = List.of(
            Fragment.constant(NodeType.NULL, "null"),
            Fragment.constant(NodeType.BOOLEAN, "true"),
            Fragment.constant(NodeType.BOOLEAN, "false")
    );

    Fragment HASH_BRACE = dataItemHash.named(DataItemType.DATA_ELEMENT.getSymbol());
    Fragment A_BRACE = dataItemA.named(DataItemType.ATTRIBUTE.getSymbol());
    Fragment C_BRACE = item(DataItemType.CONSTANT, UID);
    Fragment D_BRACE = item(DataItemType.PROGRAM_DATA_ELEMENT, UID, UID);
    Fragment I_BRACE = item(DataItemType.PROGRAM_INDICATOR, UID);
    Fragment R_BRACE = item(DataItemType.REPORTING_RATE, UID, IDENTIFIER.as(Nodes.ReportingRateTypeNode::new));
    Fragment OUG_BRACE = item(DataItemType.ORG_UNIT_GROUP, UID);
    Fragment N_BRACE = item(DataItemType.INDICATOR, UID);
    Fragment V_BRACE = variable(DataItemType.PROGRAM_VARIABLE, IDENTIFIER.as(Nodes.ProgramVariableNode::new));

    List<Fragment> CommonDataItems = List.of( HASH_BRACE, A_BRACE, C_BRACE, D_BRACE, I_BRACE, R_BRACE, OUG_BRACE );

    /*
    Modes
     */

    List<Fragment> ValidationRuleExpressionMode = concat(
            CommonFunctions,
            CommonDataItems,
            CommonConstants,
            ValidationRuleFunctions);

    List<Fragment> PredictorExpressionMode = concat(
            ValidationRuleExpressionMode,
            CommonAggregationFunctions,
            PredictorAggregationFunctions,
            List.of(MIN_DATE, MAX_DATE));

    List<Fragment> IndicatorExpressionMode = concat(
            CommonFunctions,
            CommonDataItems,
            CommonConstants,
            List.of(N_BRACE, SUB_EXPRESSION, AGGREGATION_TYPE, MIN_DATE, MAX_DATE, PERIOD_OFFSET, YEAR_TO_DATE));

    List<Fragment> PredictorSkipTestMode = PredictorExpressionMode;

    List<Fragment> SimpleTestMode = concat(CommonFunctions, CommonConstants, List.of(C_BRACE));

    List<Fragment> ProgramIndicatorExpressionMode = concat(
            CommonFunctions,
            CommonConstants,
            CommonD2Functions,
            CommonAggregationFunctions,
            ProgramIndicatorD2Functions,
            List.of(HASH_BRACE, A_BRACE, C_BRACE, V_BRACE, STAGE_OFFSET));

    List<Fragment> RuleEngineMode = concat(
            CommonConstants,
            CommonD2Functions,
            RuleEngineD2Functions,
            List.of(HASH_BRACE, A_BRACE, C_BRACE, V_BRACE));

    /*
    Block expressions
     */

    static Fragment mod(DataItemModifier modifier, Fragment... args )
    {
        String name = modifier.name();
        return block( NodeType.MODIFIER, name, '(',',', ')', args ).named(name);
    }

    static Fragment fn(NamedFunction function, Fragment... args )
    {
        String name = function.getName();
        return block( NodeType.FUNCTION, name, '(',',', ')', args ).named(name);
    }

    static Fragment item(DataItemType value, Fragment... args)
    {
        String symbol = value.getSymbol();
        return block(NodeType.DATA_ITEM, symbol, '{', '.','}', args).named(symbol);
    }

    static Fragment variable(DataItemType value, Fragment... args)
    {
        String symbol = value.getSymbol();
        return block(NodeType.VARIABLE, symbol, '{', '.','}', args).named(symbol);
    }

    static Fragment block(NodeType type, String name, char start, char argsSeparator, char end, Fragment... args )
    {
        return ( expr, ctx ) -> {
            expr.expect(start);
            ctx.beginNode( type, name );
            for ( int i = 0; i < args.length || args.length > 0 && args[args.length-1].isVarargs(); i++ )
            {
                expr.skipWS();
                Fragment arg = args[Math.min(i, args.length-1)];
                char c = expr.peek();
                if ( c == end )
                {
                    if ( arg.isMaybe() || args[args.length-1].isVarargs() )
                    {
                        ctx.endNode(type);
                        expr.expect(end);
                        return;
                    }
                    expr.error( "Expected more arguments: "
                            + Stream.of(args).skip(i).map(a -> a.name() == null ? "?" : a.name()).collect(joining(",")) );
                }
                if (i > 0) {
                    if (c != argsSeparator)
                        expr.error(format("Expected %s or %s", argsSeparator, end));
                    expr.gobble(); // separator
                    expr.skipWS();
                }
                boolean wrapInArgument = type != NodeType.VARIABLE;
                if (wrapInArgument)
                    ctx.beginNode( NodeType.ARGUMENT, "" + i );
                arg.parse( expr, ctx );
                if (wrapInArgument)
                    ctx.endNode(NodeType.ARGUMENT);
            }
            ctx.endNode(type);
            expr.expect(end);
        };
    }

    @SafeVarargs
    static List<Fragment> concat(List<Fragment>... nonTerminals) {
        return Stream.of(nonTerminals)
                .flatMap(Collection::stream)
                .collect(toUnmodifiableList());
    }
}
