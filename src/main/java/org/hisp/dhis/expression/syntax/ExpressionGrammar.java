package org.hisp.dhis.expression.syntax;

import org.hisp.dhis.expression.ast.DataItemModifier;
import org.hisp.dhis.expression.ast.NamedFunction;
import org.hisp.dhis.expression.ast.NodeType;
import org.hisp.dhis.expression.ast.Nodes;
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
 * The language is composed out of {@link Terminal}s and named {@link NonTerminal}s.
 *
 * @author Jan Bernitt
 */
public interface ExpressionGrammar
{
    /*
    Terminals (basic building blocks)
     */

    Terminal
            STRING     = () -> NodeType.STRING,
            INTEGER    = () -> NodeType.INTEGER,
            UID        = () -> NodeType.UID,
            IDENTIFIER = () -> NodeType.IDENTIFIER,
            DATE       = () -> NodeType.DATE;

    /*
    Essential Non-Terminals
     */

    NonTerminal
            expr = Expr::expr,
            dataItem = Expr::dataItem,
            dataItemHash = Expr::dataItemHash,
            dataItemA = Expr::dataItemA;

    /*
    Production Rules
     */

    List<NonTerminal> Modifiers = List.of( // (alphabetical)
            mod( DataItemModifier.aggregationType, IDENTIFIER.as(Nodes.AggregationTypeNode::new) ),
            mod( DataItemModifier.maxDate, DATE),
            mod( DataItemModifier.minDate, DATE),
            mod( DataItemModifier.periodOffset, INTEGER),
            mod( DataItemModifier.stageOffset, INTEGER),
            mod( DataItemModifier.yearToDate));

    List<NonTerminal> BaseFunctions = List.of( // (alphabetical)
            fn( NamedFunction.firstNonNull , expr.plus() ),
            fn( NamedFunction.greatest , expr.plus() ),
            fn( NamedFunction.ifThenElse , expr, expr, expr ),
            fn( NamedFunction.isNotNull , expr ),
            fn( NamedFunction.isNull , expr ),
            fn( NamedFunction.least , expr.plus() ),
            fn( NamedFunction.log , expr, expr.maybe() ),
            fn( NamedFunction.log10 , expr ),
            fn( NamedFunction.orgUnit_ancestor , UID.plus() ),
            fn( NamedFunction.orgUnit_dataSet , UID.plus() ),
            fn( NamedFunction.orgUnit_group , UID.plus() ),
            fn( NamedFunction.orgUnit_program , UID.plus() ),
            fn( NamedFunction.removeZeros , expr ),
            //TODO make mod?
            fn( NamedFunction.subExpression , expr )
    );

    List<NonTerminal> AggregationFunctions = List.of( // (alphabetical)
            fn( NamedFunction.avg, expr ),
            fn( NamedFunction.count, expr),
            fn( NamedFunction.max , expr ),
            fn( NamedFunction.median , expr ),
            fn( NamedFunction.min , expr ),
            fn( NamedFunction.percentileCont , expr, expr ),
            fn( NamedFunction.stddev , expr ),
            fn( NamedFunction.stddevPop , expr ),
            fn( NamedFunction.stddevSamp , expr ),
            fn( NamedFunction.sum , expr ),
            fn( NamedFunction.variance , expr )
    );

    List<NonTerminal> ProgramFunctions = List.of( // (alphabetical)
            fn( NamedFunction.d2_addDays , expr, expr ),
            fn( NamedFunction.d2_ceil , expr ),
            fn( NamedFunction.d2_concatenate , expr.plus() ),
            fn( NamedFunction.d2_condition , STRING, expr, expr ),
            fn( NamedFunction.d2_count , dataItem),
            fn( NamedFunction.d2_countIfCondition , expr, STRING),
            fn( NamedFunction.d2_countIfValue , dataItem, expr ),
            fn( NamedFunction.d2_countIfZeroPos , dataItem),
            fn( NamedFunction.d2_daysBetween , expr, expr ),
            fn( NamedFunction.d2_extractDataMatrixValue , expr, expr ),
            fn( NamedFunction.d2_floor , expr ),
            fn( NamedFunction.d2_hasUserRole , expr ),
            fn( NamedFunction.d2_hasValue , dataItem),
            fn( NamedFunction.d2_inOrgUnitGroup , expr ),
            fn( NamedFunction.d2_lastEventDate , expr ),
            fn( NamedFunction.d2_left , expr, expr ),
            fn( NamedFunction.d2_length , expr ),
            fn( NamedFunction.d2_maxValue , dataItem),
            fn( NamedFunction.d2_minutesBetween , expr, expr ),
            fn( NamedFunction.d2_minValue , dataItem),
            fn( NamedFunction.d2_modulus , expr, expr ),
            fn( NamedFunction.d2_monthsBetween , expr, expr ),
            fn( NamedFunction.d2_oizp , expr ),
            fn( NamedFunction.d2_relationshipCount , UID.quoted().maybe() ),
            fn( NamedFunction.d2_right , expr, expr ),
            fn( NamedFunction.d2_round , expr, INTEGER.maybe() ),
            fn( NamedFunction.d2_split , expr, expr, expr ),
            fn( NamedFunction.d2_substring , expr, expr, expr ),
            fn( NamedFunction.d2_validatePattern , expr, expr ),
            fn( NamedFunction.d2_weeksBetween , expr, expr ),
            fn( NamedFunction.d2_yearsBetween , expr, expr ),
            fn( NamedFunction.d2_zing , expr ),
            fn( NamedFunction.d2_zpvc , expr.plus() ),
            fn( NamedFunction.d2_zScoreHFA , expr, expr, expr ),
            fn( NamedFunction.d2_zScoreWFA , expr, expr, expr ),
            fn( NamedFunction.d2_zScoreWFH , expr, expr, expr )
    );

    List<NonTerminal> DataItems = List.of( // (alphabetical)
            dataItemHash.named(DataItemType.DATA_ELEMENT.getSymbol()),
            dataItemA.named(DataItemType.ATTRIBUTE.getSymbol()),
            item(DataItemType.CONSTANT, UID),
            item(DataItemType.PROGRAM_DATA_ELEMENT, UID, UID),
            item(DataItemType.PROGRAM_INDICATOR, UID),
            item(DataItemType.INDICATOR, UID),
            item(DataItemType.REPORTING_RATE, UID, IDENTIFIER.as(Nodes.ReportingRateTypeNode::new)),
            item(DataItemType.ORG_UNIT_GROUP, UID)
    );

    List<NonTerminal> Variables = List.of(
            var(DataItemType.PROGRAM_VARIABLE, IDENTIFIER.as(Nodes.ProgramVariableNode::new))
    );

    List<NonTerminal> Functions = Stream.of(BaseFunctions, AggregationFunctions, ProgramFunctions)
            .flatMap(Collection::stream)
            .collect(toUnmodifiableList());

    List<NonTerminal> Constants = List.of(
            NonTerminal.constant(NodeType.NULL, "null"),
            NonTerminal.constant(NodeType.BOOLEAN, "true"),
            NonTerminal.constant(NodeType.BOOLEAN, "false")
    );

    List<NonTerminal> AllFragments = Stream.of(Modifiers, Functions, Constants, DataItems, Variables)
            .flatMap(Collection::stream)
            .collect(toUnmodifiableList());

    // Parse MOdes
    // 1. Description ( subst. UID with name of items)
    // 2. Find data items to load from database (indicators, validation rules, predictors)
    // 3. evaluate the expression value (plug in 2.)
    // 4. Translate to SQL

    static NonTerminal mod(DataItemModifier modifier, NonTerminal... args )
    {
        String name = modifier.name();
        return block( NodeType.MODIFIER, name, '(',',', ')', args ).named(name);
    }

    static NonTerminal fn(NamedFunction function, NonTerminal... args )
    {
        String name = function.getName();
        return block( NodeType.FUNCTION, name, '(',',', ')', args ).named(name);
    }

    static NonTerminal item(DataItemType value, NonTerminal... args)
    {
        String symbol = value.getSymbol();
        return block(NodeType.DATA_ITEM, symbol, '{', '.','}', args).named(symbol);
    }

    static NonTerminal var(DataItemType value, NonTerminal... args)
    {
        String symbol = value.getSymbol();
        return block(NodeType.VARIABLE, symbol, '{', '.','}', args).named(symbol);
    }

    static NonTerminal block(NodeType type, String name, char start, char argsSeparator, char end, NonTerminal... args )
    {
        return ( expr, ctx ) -> {
            expr.expect(start);
            ctx.beginNode( type, name );
            for ( int i = 0; i < args.length || args.length > 0 && args[args.length-1].isVarargs(); i++ )
            {
                expr.skipWS();
                NonTerminal arg = args[Math.min(i, args.length-1)];
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
}
