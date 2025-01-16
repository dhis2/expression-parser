package org.hisp.dhis.lib.expression.ast

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TypedTest {

    @Test
    fun testToNumberTypeCoercion_Date() {
        val date : LocalDate = LocalDate.fromEpochDays(20)
        assertEquals(date.toEpochDays().toDouble(), Typed.toNumberTypeCoercion(date))
    }
}