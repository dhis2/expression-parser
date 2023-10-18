package org.hisp.dhis.lib.expression.function

import kotlinx.datetime.LocalDate
import org.hisp.dhis.lib.expression.spi.ValueType
import org.hisp.dhis.lib.expression.spi.VariableValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Test of the `d2:lastEventDate` function.
 *
 * @author Jan Bernitt
 */
internal class LastEventDateTest : AbstractVariableBasedTest(){

    @Test
    fun testLastEventDate_Undefined() {
        val values = mapOf("v1" to VariableValue(ValueType.DATE))
        assertNull(evaluate("d2:lastEventDate(#{v1})", values))
    }

    @Test
    fun testLastEventDate() {
        val values = mapOf("v1" to VariableValue(ValueType.DATE).copy(eventDate = "2022-01-01"))
        assertEquals(LocalDate.parse("2022-01-01"), evaluate("d2:lastEventDate(#{v1})", values))
    }
}