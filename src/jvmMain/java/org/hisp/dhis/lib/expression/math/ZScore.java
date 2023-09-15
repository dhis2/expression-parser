package org.hisp.dhis.lib.expression.math;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Map;
import java.util.Set;

import static java.lang.Double.parseDouble;

/**
 * @author Zubair Asghar (original in rule engine)
 * @author Jan Bernitt (imported into expression parser)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZScore {

    @RequiredArgsConstructor
    public enum Mode {
        WFA(ZScoreTable.Z_SCORE_WFA_TABLE_BOY, ZScoreTable.Z_SCORE_WFA_TABLE_GIRL),
        HFA(ZScoreTable.Z_SCORE_HFA_TABLE_BOY, ZScoreTable.Z_SCORE_HFA_TABLE_GIRL),
        WFH(ZScoreTable.Z_SCORE_WFH_TABLE_BOY, ZScoreTable.Z_SCORE_WFH_TABLE_GIRL);

        final Map<ZScoreTable.Key, ZScoreTable.Entry> boy;
        final Map<ZScoreTable.Key, ZScoreTable.Entry> girl;
    }

    public static double value(Mode mode, Number parameter, Number weight, String gender) {
        if (gender == null) {
            throw new IllegalArgumentException("Gender cannot be null");
        }
        return getZScore(mode, parameter.floatValue(), weight.floatValue(), GENDER_CODES.contains(gender) ? 0 : 1);
    }

    private static final Set<String> GENDER_CODES = Set.of("male", "MALE", "Male", "ma", "m", "M", "0", "false");

    private static double getZScore(Mode mode, float parameter, float weight, int gender) {
        ZScoreTable.Key key = new ZScoreTable.Key(gender, parameter);

        ZScoreTable.Entry table = gender == 1 ? mode.girl.get(key) : mode.boy.get(key);

        if (table == null) {
            throw new IllegalArgumentException("No key exist for provided parameters");
        }

        int multiplicationFactor = getMultiplicationFactor(table, weight);

        // weight exactly matches with any of the SD values
        if (table.getSdMap().containsKey(weight)) {
            int sd = table.getSdMap().get(weight);

            return (double) sd * multiplicationFactor;
        }

        // weight is beyond -3SD or 3SD
        if (weight > table.getMax()) {
            return 3.5d;
        } else if (weight < table.getMin()) {
            return -3.5d;
        }

        float lowerLimitX = 0, higherLimitY = 0;

        // find the interval
        for (float f : table.getSortedKeys()) {
            if (weight > f) {
                lowerLimitX = f;
                continue;
            }

            higherLimitY = f;
            break;
        }

        float distance = higherLimitY - lowerLimitX;

        float gap;

        float decimalAddition;

        float result;

        if (weight > findMedian(table)) {
            gap = weight - lowerLimitX;
            decimalAddition = gap / distance;
            result = table.getSdMap().get(lowerLimitX) + decimalAddition;
        } else {
            gap = higherLimitY - weight;
            decimalAddition = gap / distance;
            result = table.getSdMap().get(higherLimitY) + decimalAddition;
        }

        result = result * multiplicationFactor;

        return parseDouble(getDecimalFormat().format(result));
    }

    private static DecimalFormat getDecimalFormat() {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setDecimalSeparator('.');
        return new DecimalFormat("##0.00", symbols);
    }

    private static int getMultiplicationFactor(ZScoreTable.Entry table, float weight) {
        return Float.compare(weight, findMedian(table));
    }

    private static float findMedian(ZScoreTable.Entry table) {
        return table.getSortedKeys().get(3);
    }
}
