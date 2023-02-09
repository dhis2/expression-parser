package org.hisp.dhis.lib.expression.math;

import java.util.HashMap;
import java.util.Map;

import static org.hisp.dhis.lib.expression.math.GS1Elements.BEST_BEFORE_DATE;
import static org.hisp.dhis.lib.expression.math.GS1Elements.CONTENT;
import static org.hisp.dhis.lib.expression.math.GS1Elements.DUE_DATE;
import static org.hisp.dhis.lib.expression.math.GS1Elements.EXP_DATE;
import static org.hisp.dhis.lib.expression.math.GS1Elements.GTIN;
import static org.hisp.dhis.lib.expression.math.GS1Elements.PACK_DATE;
import static org.hisp.dhis.lib.expression.math.GS1Elements.PROD_DATE;
import static org.hisp.dhis.lib.expression.math.GS1Elements.SELL_BY;
import static org.hisp.dhis.lib.expression.math.GS1Elements.SSCC;
import static org.hisp.dhis.lib.expression.math.GS1Elements.VARIANT;

final class GS1Formatter
{
    private GS1Formatter() {
        throw new UnsupportedOperationException("util");
    }

    public static String format( String value, GS1Elements valueToReturn)
    {
        if( value == null )
            throw new IllegalArgumentException( "Can't extract data from null value" );

        if( value.length() < 3 )
            throw new IllegalArgumentException( "Value does not contains enough information" );

        String gs1Identifier = value.substring( 0, 3 );
        switch ( GS1Elements.fromKey( gs1Identifier ) ){
            case GS1_d2_IDENTIFIER:
            case GS1_Q3_IDENTIFIER:
                return formatValue(value, valueToReturn);
            case GS1_J1_IDENTIFIER:
            case GS1_d1_IDENTIFIER:
            case GS1_Q1_IDENTIFIER:
            case GS1_E0_IDENTIFIER:
            case GS1_E1_IDENTIFIER:
            case GS1_E2_IDENTIFIER:
            case GS1_E3_IDENTIFIER:
            case GS1_E4_IDENTIFIER:
            case GS1_I1_IDENTIFIER:
            case GS1_C1_IDENTIFIER:
            case GS1_e0_IDENTIFIER:
            case GS1_e1_IDENTIFIER:
            case GS1_e2_IDENTIFIER:
                throw new IllegalArgumentException( String.format( "gs1 identifier %s is not supported", gs1Identifier ) );
            default:
                throw new IllegalArgumentException( "Value does not start with a gs1 identifier" );
        }
    }

    private static String removeGS1Identifier( String value )
    {
        return value.substring( 3 );
    }

    private static String formatValue( String value, GS1Elements valueToReturn )
    {
        Map<String, String> dataMap = new HashMap<>();
        String[] gs1Groups = removeGS1Identifier( value ).split( GS1Elements.GS1_GROUP_SEPARATOR.getElement() );
        for ( String gs1Group : gs1Groups )
        {
            handleGroupData( gs1Group, dataMap );
        }
        if ( dataMap.containsKey( valueToReturn.getElement() ) )
        {
            return dataMap.get( valueToReturn.getElement() );
        }
        else
        {
            throw new IllegalArgumentException( "Required key does not exist for provided value" );
        }
    }

    private static void handleGroupData( String gs1Group, Map<String, String> dataMap )
    {
        if ( !gs1Group.isEmpty() )
        {
            int gs1GroupLength = gs1Group.length();
            String ai = GS1Elements.getApplicationIdentifier( gs1Group );
            Integer nextValueLength = AI_FIXED_LENGTH.get( ai.substring(0, 2) );
            if ( nextValueLength == null )
                nextValueLength = gs1GroupLength;
            dataMap.put( ai, gs1Group.substring( ai.length(), nextValueLength ) );
            handleGroupData( gs1Group.substring( nextValueLength ), dataMap );
        }
    }

    private static final Map<String, Integer> AI_FIXED_LENGTH = new HashMap<>();
    static {
        AI_FIXED_LENGTH.put(SSCC.getElement(), 20);
        AI_FIXED_LENGTH.put(GTIN.getElement(), 16);
        AI_FIXED_LENGTH.put(CONTENT.getElement(), 16);
        AI_FIXED_LENGTH.put("03", 16);
        AI_FIXED_LENGTH.put("04", 18);
        AI_FIXED_LENGTH.put(PROD_DATE.getElement(), 8);
        AI_FIXED_LENGTH.put(DUE_DATE.getElement(), 8);
        AI_FIXED_LENGTH.put(PACK_DATE.getElement(), 8);
        AI_FIXED_LENGTH.put("14", 8);
        AI_FIXED_LENGTH.put(BEST_BEFORE_DATE.getElement(), 8);
        AI_FIXED_LENGTH.put(SELL_BY.getElement(), 8);
        AI_FIXED_LENGTH.put(EXP_DATE.getElement(), 8);
        AI_FIXED_LENGTH.put("18", 8);
        AI_FIXED_LENGTH.put("19", 8);
        AI_FIXED_LENGTH.put(VARIANT.getElement(), 4);
        AI_FIXED_LENGTH.put("31", 10);
        AI_FIXED_LENGTH.put("32", 10);
        AI_FIXED_LENGTH.put("33", 10);
        AI_FIXED_LENGTH.put("34", 10);
        AI_FIXED_LENGTH.put("35", 10);
        AI_FIXED_LENGTH.put("36", 10);
        AI_FIXED_LENGTH.put("41", 16);
    }
}
