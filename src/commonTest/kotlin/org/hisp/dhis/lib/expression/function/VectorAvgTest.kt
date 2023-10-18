package org.hisp.dhis.lib.expression.function

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the `avg` function
 *
 * @author Jan Bernitt
 */
internal class VectorAvgTest : AbstractVectorBasedTest() {

    @Test
    fun testAvg_Single() {
        val dataValues = mapOf(
            newDeDataItem("u1234567890") to doubleArrayOf(0.0, 10.0, 5.0, 3.0, 7.0))
        assertEquals(5.0, evaluate("avg(#{u1234567890})", dataValues))
    }

    @Test
    fun testAvg_Complex() {
        val dataValues = mapOf(
            newDeDataItem("u1234567890") to doubleArrayOf(0.0, 10.0, 5.0, 3.0, 7.0),
            newDeDataItem("v1234567890") to doubleArrayOf(0.0, 10.0, 5.0, 3.0, 7.0))
        assertEquals(10.0, evaluate("avg(#{u1234567890} + #{v1234567890})", dataValues))
    }
}