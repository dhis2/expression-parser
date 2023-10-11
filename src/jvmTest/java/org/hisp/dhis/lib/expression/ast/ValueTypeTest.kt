package org.hisp.dhis.lib.expression.ast

import org.hisp.dhis.lib.expression.spi.ValueType
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests [ValueType] logic.
 *
 * @author Jan Bernitt
 */
internal class ValueTypeTest {
    /*
    Type Assignability
     */
    @Test
    fun testExactTypesAreAssignableToThemselves() {
        assertIsAssignableTo { type: ValueType -> assertTrue(type.isAssignableTo(type)) }
    }

    @Test
    fun testMixedIsAssignableToAnyType() {
        assertIsAssignableTo { type: ValueType -> assertTrue(ValueType.MIXED.isAssignableTo(type)) }
    }

    @Test
    fun testAnyTypeIsAssignableToMixed() {
        assertIsAssignableTo { type: ValueType -> assertTrue(type.isAssignableTo(ValueType.MIXED)) }
    }

    @Test
    fun testAnyTypeIsAssignableToSame() {
        assertIsAssignableTo { type: ValueType -> assertTrue(type.isAssignableTo(ValueType.SAME)) }
    }

    /**
     * Same as in input type is not valid. It resolves to any of the other types during type-checking.
     */
    @Test
    fun testSameIsNotAssignableToAnyOtherType() {
        assertIsAssignableTo { type: ValueType -> assertFalse(ValueType.SAME.isAssignableTo(type)) }
    }

    @Test
    fun testDateIsNotAssignableToNumber() {
        assertFalse(ValueType.DATE.isAssignableTo(ValueType.NUMBER))
    }

    @Test
    fun testNumberIsNotAssignableToDate() {
        assertFalse(ValueType.NUMBER.isAssignableTo(ValueType.DATE))
    }

    /*
    Type Coercion (bit like dynamic types in JS)
     */
    @Test
    fun testAnyTypeIsTypeCoercionToString() {
        assertIsAssignableTo { type: ValueType -> assertTrue(type.isMaybeAssignableTo(ValueType.STRING)) }
    }

    @Test
    fun testStringIsTypeCoercionToNumber() {
        assertTrue(ValueType.STRING.isMaybeAssignableTo(ValueType.NUMBER))
    }

    @Test
    fun testStringIsTypeCoercionToDate() {
        assertTrue(ValueType.STRING.isMaybeAssignableTo(ValueType.DATE))
    }

    @Test
    fun testNumberIsNoTypeCoercionToDate() {
        assertFalse(ValueType.NUMBER.isMaybeAssignableTo(ValueType.DATE))
    }

    companion object {
        private fun assertIsAssignableTo(test: (ValueType) -> Unit) {
            ValueType.entries
                .filter { type -> type !== ValueType.SAME }
                .forEach(test)
        }
    }
}
