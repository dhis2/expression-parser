package org.hisp.dhis.lib.expression.function

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the `count` function
 *
 * @author Jan Bernitt
 */
internal class VectorCountTest : AbstractVectorBasedTest() {

    @Test
    fun testCount() {
        val dataValues = mapOf(
            newDeDataItem("u1234567890") to doubleArrayOf(0.0, 20.0, 5.0, 3.0, 7.0))
        assertEquals(5.0, evaluate("count(#{u1234567890})", dataValues))
    }
}