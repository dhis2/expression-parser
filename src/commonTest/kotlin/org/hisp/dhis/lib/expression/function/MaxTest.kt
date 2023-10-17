package org.hisp.dhis.lib.expression.function

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the `max` function
 *
 * @author Jan Bernitt
 */
internal class MaxTest : AbstractAggregateFunctionTest() {

    @Test
    fun testMax() {
        val dataValues = mapOf(
            newDeDataItem("u1234567890") to doubleArrayOf(0.0, 10.0, 5.0, 3.0, 7.0))
        assertEquals(10.0, evaluate("max(#{u1234567890})", dataValues))
    }
}