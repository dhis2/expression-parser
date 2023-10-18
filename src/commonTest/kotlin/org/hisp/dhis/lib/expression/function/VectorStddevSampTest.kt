package org.hisp.dhis.lib.expression.function

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the `stddev` and `stddevSamp` function (same function, alias names)
 *
 * @author Jan Bernitt
 */
internal class VectorStddevSampTest : AbstractVectorBasedTest() {

    @Test
    fun testStddev() {
        val dataValues = mapOf(
            newDeDataItem("u1234567890") to doubleArrayOf(2.0, 3.0, 3.0, 8.0, 10.0, 10.0))
        assertEquals(3.7416573867739413, evaluate("stddev(#{u1234567890})", dataValues))
    }

    @Test
    fun testStddevSamp() {
        val dataValues = mapOf(
            newDeDataItem("u1234567890") to doubleArrayOf(2.0, 3.0, 3.0, 8.0, 10.0, 10.0))
        assertEquals(3.7416573867739413, evaluate("stddevSamp(#{u1234567890})", dataValues))
    }
}