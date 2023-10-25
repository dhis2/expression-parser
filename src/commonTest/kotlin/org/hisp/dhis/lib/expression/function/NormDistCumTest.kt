package org.hisp.dhis.lib.expression.function

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the `normDistCum` function
 *
 * @author Jan Bernitt
 */
internal class NormDistCumTest : AbstractVectorBasedTest() {

    @Test
    fun testNormDistCum() {
        val dataValues = mapOf(
            newDeDataItem("u1234567890") to doubleArrayOf(5.0, 0.0, 10.0, 3.0, 7.0))
        // probability of a value x being left of 5.0 (first value) for the given population
        // as the mean is 5.0 the probability to be left of the mean is exactly 0.5 or 50%
        assertEquals(0.5, evaluate("normDistCum(#{u1234567890})", dataValues))
    }
}