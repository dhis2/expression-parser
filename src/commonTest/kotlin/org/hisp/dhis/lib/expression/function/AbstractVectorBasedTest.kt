package org.hisp.dhis.lib.expression.function

import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.spi.*

abstract class AbstractVectorBasedTest {

    fun evaluate(expression: String, dataValues: Map<DataItem, Any>): Double? {
        return Expression(expression).evaluate(
            { _: String -> null },
            ExpressionData().copy(dataItemValues = dataValues)) as Double?
    }

    fun newDeDataItem(u1234567890: String): DataItem {
        return DataItem(
            DataItemType.DATA_ELEMENT, ID(ID.Type.DataElementUID, u1234567890),
            QueryModifiers().copy(periodAggregation = true))
    }
}