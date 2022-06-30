package org.hisp.dhis.expression;

import org.hisp.dhis.expression.parse.NonTerminal;

import java.util.List;

@SuppressWarnings("java:S115")
public enum NamedFunction
{
    // Base Functions
    firstNonNull("firstNonNull"),
    greatest("greatest"),
    ifThenElse("if"),
    isNotNull("isNotNull"),
    isNull("isNull"),
    least("least"),
    log("log"),
    log10("log10"),
    orgUnit_ancestor("orgUnit.ancestor"),
    orgUnit_dataSet("orgUnit.dataSet"),
    orgUnit_group("orgUnit.group"),
    orgUnit_program("orgUnit.program"),
    subExpression("subExpression"),

    // Aggregation Functions
    avg("avg"),
    count("count"),
    max("max"),
    median("median"),
    min("min"),
    percentileCont("percentileCont"),
    stddev("stddev"),
    stddevPop("stddevPop"),
    stddevSamp("stddevSamp"),
    sum("sum"),
    variance("variance"),

    // Program Functions
    d2_addDays("d2:addDays"),
    d2_ceil("d2:ceil"),
    d2_concatenate("d2:concatenate"),
    d2_condition("d2:condition"),
    d2_count("d2:count"),
    d2_countIfCondition("d2:countIfCondition"),
    d2_countIfValue("d2:countIfValue"),
    d2_countIfZeroPos("d2:countIfZeroPos"),
    d2_daysBetween("d2:daysBetween"),
    d2_extractDataMatrixValue("d2:extractDataMatrixValue"),
    d2_floor("d2:floor"),
    d2_hasUserRole("d2:hasUserRole"),
    d2_hasValue("d2:hasValue"),
    d2_inOrgUnitGroup("d2:inOrgUnitGroup"),
    d2_lastEventDate("d2:lastEventDate"),
    d2_left("d2:left"),
    d2_length("d2:length"),
    d2_maxValue("d2:maxValue"),
    d2_minutesBetween("d2:minutesBetween"),
    d2_minValue("d2:minValue"),
    d2_modulus("d2:modulus"),
    d2_monthsBetween("d2:monthsBetween"),
    d2_oizp("d2:oizp"),
    d2_relationshipCount("d2:relationshipCount"),
    d2_right("d2:right"),
    d2_round("d2:round"),
    d2_split("d2:split"),
    d2_substring("d2:substring"),
    d2_validatePattern("d2:validatePattern"),
    d2_weeksBetween("d2:weeksBetween"),
    d2_yearsBetween("d2:yearsBetween"),
    d2_zing("d2:zing"),
    d2_zpvc("d2:zpvc"),
    d2_zScoreHFA("d2:zScoreHFA"),
    d2_zScoreWFA("d2:zScoreWFA"),
    d2_zScoreWFH("d2:zScoreWFH");

    private final String name;

    NamedFunction(String name) {

        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Avoid defensive copy when finding function by name
     */
    private static final List<NamedFunction> VALUES = List.of(values());

    static NamedFunction fromName(String name) {
        return VALUES.stream().filter(op -> op.name.equals(name)).findFirst().orElseThrow();
    }
}
