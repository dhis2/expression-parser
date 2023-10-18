package org.hisp.dhis.lib.expression.function

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the `variance` function
 *
 * @author Jan Bernitt
 */
internal class VectorVarianceTest : AbstractVectorBasedTest() {

    @Test
    fun testVariance() {
        val dataValues = mapOf(
            newDeDataItem("u1234567890") to doubleArrayOf(6.0, 7.0, 8.0, 9.0, 10.0))
        assertEquals(2.5, evaluate("variance(#{u1234567890})", dataValues))

        val dataValues2 = mapOf(
            newDeDataItem("u1234567890") to doubleArrayOf(4.0, 6.0, 8.0, 10.0, 12.0))
        assertEquals(10.0, evaluate("variance(#{u1234567890})", dataValues2))
    }
}