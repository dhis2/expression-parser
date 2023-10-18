package org.hisp.dhis.lib.expression.function

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the `percentileCont` function
 *
 * @author Jan Bernitt
 */
internal class VectorPercentileContTest : AbstractVectorBasedTest() {

    /*
    OBS! The tests only confirm the current behaviour is stable.
    Whether it is correct or not is unclear as there were no tests
     */

    @Test
    fun testPercentileCont() {
        val dataValues = mapOf(
            newDeDataItem("u1234567890") to doubleArrayOf(0.0, 20.0, 5.0, 3.0, 7.0))
        // 0.5 is median
        assertEquals(5.0, evaluate("percentileCont(#{u1234567890}, 0.5)", dataValues))
        // this should give something else
        assertEquals(3.0, evaluate("percentileCont(#{u1234567890}, 0.25)", dataValues))
    }
}