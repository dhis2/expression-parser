package org.hisp.dhis.lib.expression.function

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests the `median` function
 *
 * @author Jan Bernitt
 */
internal class MedianTest : AggregateFunctionTest() {

    @Test
    fun testMedian_Empty() {
        val dataValues = mapOf(
            newDeDataItem("u1234567890") to doubleArrayOf())
        assertNull(evaluate("median(#{u1234567890})", dataValues))
    }

    @Test
    fun testMedian_SameAsAvg() {
        val dataValues = mapOf(
            newDeDataItem("u1234567890") to doubleArrayOf(0.0, 10.0, 5.0, 3.0, 7.0))
        assertEquals(5.0, evaluate("median(#{u1234567890})", dataValues))
    }

    @Test
    fun testMedian_Odd() {
        val dataValues = mapOf(
            newDeDataItem("u1234567890") to doubleArrayOf(0.0, 10.0, 5.0, 3.0, 20.0))
        assertEquals(5.0, evaluate("median(#{u1234567890})", dataValues))

        val dataValues2 = mapOf(
            newDeDataItem("u1234567890") to doubleArrayOf(8.0, 9.0, 5.0, 1.0, 6.0))
        assertEquals(6.0, evaluate("median(#{u1234567890})", dataValues2))
    }

    @Test
    fun testMedian_Even() {
        val dataValues = mapOf(
            newDeDataItem("u1234567890") to doubleArrayOf(8.0, 9.0, 5.0, 1.0, 7.0, 2.0))
        assertEquals(6.0, evaluate("median(#{u1234567890})", dataValues))
    }

    @Test
    fun testMedian_NaN() {
        // NaN is stripped/ignored
        val dataValues = mapOf(
            newDeDataItem("u1234567890") to doubleArrayOf(Double.NaN, 8.0, 9.0, 5.0, 1.0, 7.0, 2.0))
        assertEquals(6.0, evaluate("median(#{u1234567890})", dataValues))

        // if only NaN the result is also NaN
        val dataValues2 = mapOf(
            newDeDataItem("u1234567890") to doubleArrayOf(Double.NaN, Double.NaN))
        assertEquals(Double.NaN, evaluate("median(#{u1234567890})", dataValues2))
    }

    @Test
    fun testMedian_Infinity() {
        val dataValues = mapOf(
            newDeDataItem("u1234567890") to doubleArrayOf(Double.POSITIVE_INFINITY, 8.0, 9.0, 5.0, 1.0, 7.0, 2.0))
        assertEquals(7.0, evaluate("median(#{u1234567890})", dataValues))
    }
}