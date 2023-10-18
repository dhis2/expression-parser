package org.hisp.dhis.lib.expression.function

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the `stddevPop` function
 *
 * @author Jan Bernitt
 */
internal class VectorStddevPopTest : AbstractVectorBasedTest() {

    @Test
    fun testStddevPop() {
        val dataValues = mapOf(
            newDeDataItem("u1234567890") to doubleArrayOf(2.0, 3.0, 3.0, 8.0, 10.0, 10.0))
        assertEquals(3.415650255319866, evaluate("stddevPop(#{u1234567890})", dataValues))
    }
}