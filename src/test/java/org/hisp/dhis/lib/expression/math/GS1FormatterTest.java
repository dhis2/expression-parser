package org.hisp.dhis.lib.expression.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Translated from rule-engine {@code GS1DataMatrixValueFormatterTest}.
 */
class GS1FormatterTest {

    @Test
    void shouldReturnGs1DataMatrixFormatter(){
        String value = "]d2\u001D01084700069915412110081996195256\u001D10DXB2005\u001D17220228";
        assertDoesNotThrow( () -> GS1Elements.GTIN.format(value));
    }

    @Test
    void shouldFormatToDataMatrixValues() {
        String value = "]d2\u001D01084700069915412110081996195256\u001D10DXB2005\u001D17220228";

        assertEquals("08470006991541", GS1Elements.GTIN.format(value) );
        assertEquals("10081996195256", GS1Elements.SERIAL_NUMBER.format(value));
        assertEquals("DXB2005", GS1Elements.LOT_NUMBER.format(value));
        assertEquals("220228", GS1Elements.EXP_DATE.format(value));
    }

    @Test
    void shouldFormatToDataMatrixValuesWithoutInitialGS() {
        String value = "]d201084700069915412110081996195256\u001D10DXB2005\u001D17220228";

        assertEquals("08470006991541", GS1Elements.GTIN.format(value));
        assertEquals("10081996195256", GS1Elements.SERIAL_NUMBER.format(value));
        assertEquals("DXB2005", GS1Elements.LOT_NUMBER.format(value));
        assertEquals("220228", GS1Elements.EXP_DATE.format(value));
    }

    @Test
    void shouldThrowExceptionIfValueIsNotAvailable() {
        String value = "]d201084700069915412110081996195256\u001D10DXB2005\u001D17220228";
        assertThrows(IllegalArgumentException.class, () -> GS1Elements.BILL_TO_LOC.format(value));
    }

    @Test
    void shouldThrowExceptionIfNoApplicationIdentifierIsFound() {
        String value = "]d2084700069915412110081996195256\u001DDXB2005\u001D220228";
        assertThrows(IllegalArgumentException.class, () -> GS1Elements.BILL_TO_LOC.format(value));
    }
}
