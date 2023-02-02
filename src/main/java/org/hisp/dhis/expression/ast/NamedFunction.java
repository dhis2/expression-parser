package org.hisp.dhis.expression.ast;

import org.hisp.dhis.expression.spi.ValueType;

import java.util.List;

import static org.hisp.dhis.expression.spi.ValueType.BOOLEAN;
import static org.hisp.dhis.expression.spi.ValueType.DATE;
import static org.hisp.dhis.expression.spi.ValueType.NUMBER;
import static org.hisp.dhis.expression.spi.ValueType.SAME;
import static org.hisp.dhis.expression.spi.ValueType.STRING;
import static org.hisp.dhis.expression.spi.ValueType.MIXED;

@SuppressWarnings("java:S115")
public enum NamedFunction implements Typed
{
    // Base Functions
    firstNonNull("firstNonNull", SAME, true, SAME),
    greatest("greatest", NUMBER, true, NUMBER),
    ifThenElse("if", SAME, BOOLEAN, SAME, SAME),
    isNotNull("isNotNull", BOOLEAN, SAME),
    isNull("isNull", BOOLEAN, SAME),
    least("least", NUMBER, true, NUMBER),
    log("log", NUMBER, NUMBER, NUMBER),
    log10("log10", NUMBER, NUMBER),
    orgUnit_ancestor("orgUnit.ancestor", BOOLEAN, true, STRING),
    orgUnit_dataSet("orgUnit.dataSet", BOOLEAN, true, STRING),
    orgUnit_group("orgUnit.group", BOOLEAN, true, STRING),
    orgUnit_program("orgUnit.program", BOOLEAN, true, STRING),
    removeZeros("removeZeros", SAME, SAME),
    subExpression("subExpression", SAME, SAME),

    // Aggregation Functions
    avg("avg", NUMBER, NUMBER),
    count("count", NUMBER, NUMBER),
    max("max", NUMBER, NUMBER),
    median("median", NUMBER, NUMBER),
    min("min", NUMBER, NUMBER),
    percentileCont("percentileCont", NUMBER, NUMBER, NUMBER),
    stddev("stddev", NUMBER, NUMBER),
    stddevPop("stddevPop", NUMBER, NUMBER),
    stddevSamp("stddevSamp", NUMBER, NUMBER),
    sum("sum", NUMBER, NUMBER),
    variance("variance", NUMBER, NUMBER),

    // Program Functions
    d2_addDays("d2:addDays", DATE, DATE, NUMBER),
    d2_ceil("d2:ceil", NUMBER, NUMBER),
    d2_concatenate("d2:concatenate", STRING, true, STRING),
    d2_condition("d2:condition", SAME, BOOLEAN, SAME, SAME),
    d2_count("d2:count", NUMBER, MIXED),
    d2_countIfCondition("d2:countIfCondition", NUMBER, BOOLEAN, MIXED),
    d2_countIfValue("d2:countIfValue", NUMBER, MIXED, MIXED),
    d2_countIfZeroPos("d2:countIfZeroPos", NUMBER, NUMBER),
    d2_daysBetween("d2:daysBetween", NUMBER, DATE, DATE),
    d2_extractDataMatrixValue("d2:extractDataMatrixValue", STRING, STRING, STRING),
    d2_floor("d2:floor", NUMBER, NUMBER),
    d2_hasUserRole("d2:hasUserRole", BOOLEAN, STRING),
    d2_hasValue("d2:hasValue", BOOLEAN, STRING),
    d2_inOrgUnitGroup("d2:inOrgUnitGroup", BOOLEAN, STRING),
    d2_lastEventDate("d2:lastEventDate", DATE, STRING),
    d2_left("d2:left", STRING, STRING, NUMBER),
    d2_length("d2:length", NUMBER, STRING),
    d2_maxValue("d2:maxValue", NUMBER, MIXED),
    d2_minutesBetween("d2:minutesBetween", NUMBER, DATE, DATE),
    d2_minValue("d2:minValue", NUMBER, MIXED),
    d2_modulus("d2:modulus", NUMBER, NUMBER, NUMBER),
    d2_monthsBetween("d2:monthsBetween", NUMBER, DATE, DATE),
    d2_oizp("d2:oizp", NUMBER, NUMBER),
    d2_relationshipCount("d2:relationshipCount", NUMBER, STRING),
    d2_right("d2:right", STRING, STRING, NUMBER),
    d2_round("d2:round", NUMBER, NUMBER, NUMBER),
    d2_split("d2:split", STRING, STRING, STRING, NUMBER),
    d2_substring("d2:substring", STRING, STRING, NUMBER, NUMBER),
    d2_validatePattern("d2:validatePattern", BOOLEAN, STRING, STRING),
    d2_weeksBetween("d2:weeksBetween", NUMBER, DATE, DATE),
    d2_yearsBetween("d2:yearsBetween", NUMBER, DATE, DATE),
    d2_zing("d2:zing", NUMBER, NUMBER),
    d2_zpvc("d2:zpvc", NUMBER, true, NUMBER),
    d2_zScoreHFA("d2:zScoreHFA", NUMBER, NUMBER, NUMBER, STRING),
    d2_zScoreWFA("d2:zScoreWFA", NUMBER, NUMBER, NUMBER, STRING),
    d2_zScoreWFH("d2:zScoreWFH", NUMBER, NUMBER, NUMBER, STRING);

    private final String name;
    private final ValueType returnType;
    private final boolean isVarargs;
    private final List<ValueType> parameterTypes;

    NamedFunction(String name, ValueType returnType, boolean isVarargs, ValueType...parameterTypes) {
        this.name = name;
        this.returnType = returnType;
        this.isVarargs = isVarargs;
        this.parameterTypes = List.of(parameterTypes);
    }

    NamedFunction(String name, ValueType returnType, ValueType...parameterTypes) {
        this(name, returnType, false, parameterTypes);
    }

    public String getName() {
        return name;
    }

    @Override
    public ValueType getValueType() {
        return returnType;
    }

    public boolean isVarargs() {
        return isVarargs;
    }

    public List<ValueType> getParameterTypes() {
        return parameterTypes;
    }

    public boolean isAggregating() {
        return ordinal() >= avg.ordinal() && ordinal() <= variance.ordinal();
    }

    /**
     * Avoid defensive copy when finding function by name
     */
    private static final List<NamedFunction> VALUES = List.of(values());

    static NamedFunction fromName(String name) {
        return VALUES.stream().filter(op -> op.name.equals(name)).findFirst().orElseThrow();
    }
}
