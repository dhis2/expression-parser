package org.hisp.dhis.lib.expression.ast

import org.hisp.dhis.lib.expression.spi.ValueType

enum class NamedFunction(
    private val fnName: String,
    private val returnType: ValueType,
    val isVarargs: Boolean,
    vararg parameterTypes: ValueType
) : Typed {
    // Base Functions
    contains("contains", ValueType.BOOLEAN, true, ValueType.STRING, ValueType.STRING, ValueType.STRING),
    containsItems("containsItems", ValueType.BOOLEAN, true, ValueType.STRING, ValueType.STRING, ValueType.STRING),
    firstNonNull("firstNonNull", ValueType.SAME, true, ValueType.SAME),
    greatest("greatest", ValueType.NUMBER, true, ValueType.NUMBER),
    ifThenElse("if", ValueType.SAME, ValueType.BOOLEAN, ValueType.SAME, ValueType.SAME),
    isNotNull("isNotNull", ValueType.BOOLEAN, ValueType.SAME),
    isNull("isNull", ValueType.BOOLEAN, ValueType.SAME),
    least("least", ValueType.NUMBER, true, ValueType.NUMBER),
    log("log", ValueType.NUMBER, ValueType.NUMBER, ValueType.NUMBER),
    log10("log10", ValueType.NUMBER, ValueType.NUMBER),
    orgUnit_ancestor("orgUnit.ancestor", ValueType.BOOLEAN, true, ValueType.STRING),
    orgUnit_dataSet("orgUnit.dataSet", ValueType.BOOLEAN, true, ValueType.STRING),
    orgUnit_group("orgUnit.group", ValueType.BOOLEAN, true, ValueType.STRING),
    orgUnit_program("orgUnit.program", ValueType.BOOLEAN, true, ValueType.STRING),
    removeZeros("removeZeros", ValueType.SAME, ValueType.SAME),
    subExpression("subExpression", ValueType.SAME, ValueType.SAME),

    // Aggregation Functions
    avg("avg", ValueType.NUMBER, ValueType.NUMBER),
    count("count", ValueType.NUMBER, ValueType.NUMBER),
    max("max", ValueType.NUMBER, ValueType.NUMBER),
    median("median", ValueType.NUMBER, ValueType.NUMBER),
    min("min", ValueType.NUMBER, ValueType.NUMBER),
    percentileCont("percentileCont", ValueType.NUMBER, ValueType.NUMBER, ValueType.NUMBER),
    stddev("stddev", ValueType.NUMBER, ValueType.NUMBER),
    stddevPop("stddevPop", ValueType.NUMBER, ValueType.NUMBER),
    stddevSamp("stddevSamp", ValueType.NUMBER, ValueType.NUMBER),
    sum("sum", ValueType.NUMBER, ValueType.NUMBER),
    variance("variance", ValueType.NUMBER, ValueType.NUMBER),

    // Somewhat aggregation functions but not quite
    normDistCum("normDistCum", ValueType.NUMBER, ValueType.NUMBER, ValueType.NUMBER, ValueType.NUMBER),
    normDistDen("normDistCum", ValueType.NUMBER, ValueType.NUMBER, ValueType.NUMBER, ValueType.NUMBER),

    // Program Functions
    d2_addDays("d2:addDays", ValueType.DATE, ValueType.DATE, ValueType.NUMBER),
    d2_ceil("d2:ceil", ValueType.NUMBER, ValueType.NUMBER),
    d2_concatenate("d2:concatenate", ValueType.STRING, true, ValueType.STRING),
    d2_condition("d2:condition", ValueType.SAME, ValueType.BOOLEAN, ValueType.SAME, ValueType.SAME),
    d2_contains("d2:contains", ValueType.BOOLEAN, true, ValueType.STRING, ValueType.STRING, ValueType.STRING),
    d2_containsItems("d2:containsItems", ValueType.BOOLEAN, true, ValueType.STRING, ValueType.STRING, ValueType.STRING),
    d2_count("d2:count", ValueType.NUMBER, ValueType.MIXED),
    d2_countIfCondition("d2:countIfCondition", ValueType.NUMBER, ValueType.BOOLEAN, ValueType.MIXED),
    d2_countIfValue("d2:countIfValue", ValueType.NUMBER, ValueType.MIXED, ValueType.MIXED),
    d2_countIfZeroPos("d2:countIfZeroPos", ValueType.NUMBER, ValueType.NUMBER),
    d2_daysBetween("d2:daysBetween", ValueType.NUMBER, ValueType.DATE, ValueType.DATE),
    d2_extractDataMatrixValue("d2:extractDataMatrixValue", ValueType.STRING, ValueType.STRING, ValueType.STRING),
    d2_floor("d2:floor", ValueType.NUMBER, ValueType.NUMBER),
    d2_hasUserRole("d2:hasUserRole", ValueType.BOOLEAN, ValueType.STRING),
    d2_hasValue("d2:hasValue", ValueType.BOOLEAN, ValueType.MIXED),
    d2_inOrgUnitGroup("d2:inOrgUnitGroup", ValueType.BOOLEAN, ValueType.STRING),
    d2_lastEventDate("d2:lastEventDate", ValueType.DATE, ValueType.STRING),
    d2_left("d2:left", ValueType.STRING, ValueType.STRING, ValueType.NUMBER),
    d2_length("d2:length", ValueType.NUMBER, ValueType.STRING),
    d2_maxValue("d2:maxValue", ValueType.NUMBER, ValueType.MIXED),
    d2_minutesBetween("d2:minutesBetween", ValueType.NUMBER, ValueType.DATE, ValueType.DATE),
    d2_minValue("d2:minValue", ValueType.NUMBER, ValueType.MIXED),
    d2_modulus("d2:modulus", ValueType.NUMBER, ValueType.NUMBER, ValueType.NUMBER),
    d2_monthsBetween("d2:monthsBetween", ValueType.NUMBER, ValueType.DATE, ValueType.DATE),
    d2_oizp("d2:oizp", ValueType.NUMBER, ValueType.NUMBER),
    d2_relationshipCount("d2:relationshipCount", ValueType.NUMBER, ValueType.STRING),
    d2_right("d2:right", ValueType.STRING, ValueType.STRING, ValueType.NUMBER),
    d2_round("d2:round", ValueType.NUMBER, ValueType.NUMBER, ValueType.NUMBER),
    d2_split("d2:split", ValueType.STRING, ValueType.STRING, ValueType.STRING, ValueType.NUMBER),
    d2_substring("d2:substring", ValueType.STRING, ValueType.STRING, ValueType.NUMBER, ValueType.NUMBER),
    d2_validatePattern("d2:validatePattern", ValueType.BOOLEAN, ValueType.STRING, ValueType.STRING),
    d2_weeksBetween("d2:weeksBetween", ValueType.NUMBER, ValueType.DATE, ValueType.DATE),
    d2_yearsBetween("d2:yearsBetween", ValueType.NUMBER, ValueType.DATE, ValueType.DATE),
    d2_zing("d2:zing", ValueType.NUMBER, ValueType.NUMBER),
    d2_zpvc("d2:zpvc", ValueType.NUMBER, true, ValueType.NUMBER),
    d2_zScoreHFA("d2:zScoreHFA", ValueType.NUMBER, ValueType.NUMBER, ValueType.NUMBER, ValueType.STRING),
    d2_zScoreWFA("d2:zScoreWFA", ValueType.NUMBER, ValueType.NUMBER, ValueType.NUMBER, ValueType.STRING),
    d2_zScoreWFH("d2:zScoreWFH", ValueType.NUMBER, ValueType.NUMBER, ValueType.NUMBER, ValueType.STRING);

    val parameterTypes: List<ValueType>

    constructor(name: String, returnType: ValueType, vararg parameterTypes: ValueType) : this(
        name,
        returnType,
        false,
        *parameterTypes
    )

    fun getName() : String {
        return fnName;
    }

    override fun getValueType(): ValueType {
        return returnType
    }

    fun isRequiringAggregationData(): Boolean {
        return ordinal >= avg.ordinal && ordinal <= normDistDen.ordinal;
    }

    fun isAggregating(): Boolean {
        return ordinal >= avg.ordinal && ordinal <= variance.ordinal;
    }

    init {
        this.parameterTypes = listOf(*parameterTypes)
    }

    companion object {

        fun fromName(name: String): NamedFunction {
            return entries.first { it.fnName == name }
        }
    }
}
