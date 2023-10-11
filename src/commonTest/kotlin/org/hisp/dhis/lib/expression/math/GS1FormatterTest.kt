package org.hisp.dhis.lib.expression.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

/**
 * Translated from rule-engine `GS1DataMatrixValueFormatterTest`.
 */
internal class GS1FormatterTest {
    @Test
    fun shouldReturnGs1DataMatrixFormatter() {
        val value = "]d2\u001D01084700069915412110081996195256\u001D10DXB2005\u001D17220228"
        assertNotNull(GS1Elements.GTIN.format(value))
    }

    @Test
    fun shouldFormatToDataMatrixValues() {
        val value = "]d2\u001D01084700069915412110081996195256\u001D10DXB2005\u001D17220228"
        assertEquals("08470006991541", GS1Elements.GTIN.format(value))
        assertEquals("10081996195256", GS1Elements.SERIAL_NUMBER.format(value))
        assertEquals("DXB2005", GS1Elements.LOT_NUMBER.format(value))
        assertEquals("220228", GS1Elements.EXP_DATE.format(value))
    }

    @Test
    fun shouldFormatToDataMatrixValuesWithoutInitialGS() {
        val value = "]d201084700069915412110081996195256\u001D10DXB2005\u001D17220228"
        assertEquals("08470006991541", GS1Elements.GTIN.format(value))
        assertEquals("10081996195256", GS1Elements.SERIAL_NUMBER.format(value))
        assertEquals("DXB2005", GS1Elements.LOT_NUMBER.format(value))
        assertEquals("220228", GS1Elements.EXP_DATE.format(value))
    }

    @Test
    fun shouldThrowExceptionIfValueIsNotAvailable() {
        val value = "]d201084700069915412110081996195256\u001D10DXB2005\u001D17220228"
        assertFailsWith(IllegalArgumentException::class) { GS1Elements.BILL_TO_LOC.format(value) }
    }

    @Test
    fun shouldThrowExceptionIfNoApplicationIdentifierIsFound() {
        val value = "]d2084700069915412110081996195256\u001DDXB2005\u001D220228"
        assertFailsWith(IllegalArgumentException::class) { GS1Elements.BILL_TO_LOC.format(value) }
    }
}
