package org.hisp.dhis.expression.parse;

import org.hisp.dhis.expression.NamedMethod;
import org.hisp.dhis.expression.NodeType;
import org.hisp.dhis.expression.Nodes;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.hisp.dhis.expression.parse.NonTerminal.oneOrMore;

public interface ExprGrammar
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
            data = Expr::data,
            dataItem = Expr::dataItem;

    /*
    Production Rules
     */

    List<NonTerminal> Methods = List.of( // (alphabetical)
            method( NamedMethod.aggregationType, IDENTIFIER ),
            method( NamedMethod.maxDate, DATE),
            method( NamedMethod.minDate, DATE),
            method( NamedMethod.periodOffset, INTEGER),
            method( NamedMethod.stageOffset, INTEGER));

    List<NonTerminal> BaseFunctions = List.of( // (alphabetical)
            fn( "firstNonNull", oneOrMore( expr ) ),
            fn( "greatest", oneOrMore( expr ) ),
            fn( "if", expr, expr, expr ),
            fn( "isNotNull", expr ),
            fn( "isNull", expr ),
            fn( "least", oneOrMore( expr ) ),
            fn( "log", expr, expr.maybe() ),
            fn( "log10", expr ),
            fn( "orgUnit.ancestor", oneOrMore( UID ) ),
            fn( "orgUnit.dataSet", oneOrMore( UID ) ),
            fn( "orgUnit.group", oneOrMore( UID ) ),
            fn( "orgUnit.program", oneOrMore( UID ) ),
            fn( "subExpression", expr )
    );

    List<NonTerminal> AggregationFunctions = List.of( // (alphabetical)
            fn( "avg", expr ),
            fn( "count", expr),
            fn( "max", expr ),
            fn( "median", expr ),
            fn( "min", expr ),
            fn( "percentileCont", expr, expr ),
            fn( "stddev", expr ),
            fn( "stddevPop", expr ),
            fn( "stddevSamp", expr ),
            fn( "sum", expr ),
            fn( "variance", expr )
    );

    List<NonTerminal> ProgramFunctions = List.of( // (alphabetical)
            fn( "d2:addDays", expr, expr ),
            fn( "d2:ceil", expr ),
            fn( "d2:concatenate", oneOrMore( expr ) ),
            fn( "d2:condition", STRING, expr, expr ),
            fn( "d2:count", dataItem),
            fn( "d2:countIfCondition", expr, STRING),
            fn( "d2:countIfValue", dataItem, expr ),
            fn( "d2:countIfZeroPos", dataItem),
            fn( "d2:daysBetween", expr, expr ),
            fn( "d2:extractDataMatrixValue", expr, expr ),
            fn( "d2:floor", expr ),
            fn( "d2:hasUserRole", expr ),
            fn( "d2:hasValue", data ),
            fn( "d2:inOrgUnitGroup", expr ),
            fn( "d2:lastEventDate", expr ),
            fn( "d2:left", expr, expr ),
            fn( "d2:length", expr ),
            fn( "d2:maxValue", dataItem),
            fn( "d2:minutesBetween", expr, expr ),
            fn( "d2:minValue", dataItem),
            fn( "d2:modulus", expr, expr ),
            fn( "d2:monthsBetween", expr, expr ),
            fn( "d2:oizp", expr ),
            fn( "d2:relationshipCount", UID.quoted() ),
            fn( "d2:right", expr, expr ),
            fn( "d2:round", expr ),
            fn( "d2:split", expr, expr, expr ),
            fn( "d2:substring", expr, expr, expr ),
            fn( "d2:validatePattern", expr, expr ),
            fn( "d2:weeksBetween", expr, expr ),
            fn( "d2:yearsBetween", expr, expr ),
            fn( "d2:zing", expr ),
            fn( "d2:zpvc", oneOrMore( expr ) ),
            fn( "d2:zScoreHFA", expr, expr, expr ),
            fn( "d2:zScoreWFA", expr, expr, expr ),
            fn( "d2:zScoreWFH", expr, expr, expr )
    );

    List<NonTerminal> DataValues = List.of( // (alphabetical)
            data("#", data),
            data("A", data),
            data("C", UID),
            data("D", UID, UID),
            data("I", UID),
            data("N", UID),
            data("R", UID, IDENTIFIER.as(Nodes.ReportingRateTypeNode::new)),
            data("V", IDENTIFIER.as(Nodes.ProgramVariableNode::new)),
            data("OUG", UID)
    );

    List<NonTerminal> Functions = Stream.of(BaseFunctions, AggregationFunctions, ProgramFunctions, DataValues)
            .flatMap(Collection::stream)
            .collect(toUnmodifiableList());

    List<NonTerminal> Constants = List.of(
            NonTerminal.constant(NodeType.NULL, "null"),
            NonTerminal.constant(NodeType.BOOLEAN, "true"),
            NonTerminal.constant(NodeType.BOOLEAN, "false")
    );

    // Parse MOdes
    // 1. Description ( subst. UID with name of items)
    // 2. Find data items to load from database (indicators, validation rules, predictors)
    // 3. evaluate the expression value (plug in 2.)
    // 4. Translate to SQL

    static NonTerminal method(NamedMethod method, NonTerminal... args )
    {
        String name = method.name();
        return call( NodeType.METHOD, name, ',', args ).inRound().named(name);
    }

    static NonTerminal fn(String name, NonTerminal... args )
    {
        return call( NodeType.FUNCTION, name, ',', args ).inRound().named( name );
    }

    static NonTerminal data(String name, NonTerminal... args)
    {
        return call(NodeType.DATA_VALUE, name, '.', args).inCurly().named(name);
    }

    static NonTerminal call(NodeType type, String name, char argsSeparator, NonTerminal... args )
    {
        return ( expr, ctx ) -> {
            ctx.beginNode( type, name );
            for ( int i = 0; i < args.length; i++ )
            {
                expr.skipWS();
                NonTerminal arg = args[i];
                if ( i > 0 )
                {
                    char c = expr.peek();
                    if ( c != argsSeparator )
                    {
                        if ( arg.isMaybe() )
                        {
                            ctx.endNode(type);
                            return;
                        }
                        expr.error( "Expected more arguments" );
                    }
                    expr.gobble(); // separator
                    expr.skipWS();
                }
                ctx.beginNode( NodeType.ARGUMENT, "" + i );
                arg.parse( expr, ctx );
                ctx.endNode(NodeType.ARGUMENT);
            }
            ctx.endNode(type);
        };
    }
}
