package org.hisp.dhis.lib.expression.ast

enum class AggregationType {
    sum,
    avg,
    avg_sum_org_unit,
    last,
    last_avg_org_unit,
    last_analytics_period,
    last_analytics_period_avg_org_unit,
    first,
    first_avg_org_unit,
    count,
    stddev,
    variance,
    min,
    max,
    none,
    custom
}
