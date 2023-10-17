package org.hisp.dhis.lib.expression.function

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the `sum` function
 *
 * @author Jan Bernitt
 */
internal class SumTest : AbstractAggregateFunctionTest() {

    @Test
    fun testSum() {
        val dataValues = mapOf(
            newDeDataItem("u1234567890") to doubleArrayOf(0.0, 10.0, 5.0, 3.0, 7.0))
        assertEquals(25.0, evaluate("sum(#{u1234567890})", dataValues))
    }
}