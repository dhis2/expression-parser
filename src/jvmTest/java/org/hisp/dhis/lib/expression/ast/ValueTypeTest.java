package org.hisp.dhis.lib.expression.ast;

import org.hisp.dhis.lib.expression.spi.ValueType;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link ValueType} logic.
 *
 * @author Jan Bernitt
 */
class ValueTypeTest {

    /*
    Type Assignability
     */

    @Test
    void testExactTypesAreAssignableToThemselves() {
        assertIsAssignableTo(type -> assertTrue(type.isAssignableTo(type)));
    }

    @Test
    void testMixedIsAssignableToAnyType() {
        assertIsAssignableTo(type -> assertTrue(ValueType.MIXED.isAssignableTo(type)));
    }

    @Test
    void testAnyTypeIsAssignableToMixed() {
        assertIsAssignableTo(type -> assertTrue(type.isAssignableTo(ValueType.MIXED)));
    }

    @Test
    void testAnyTypeIsAssignableToSame() {
        assertIsAssignableTo(type -> assertTrue(type.isAssignableTo(ValueType.SAME)));
    }

    /**
     * Same as in input type is not valid. It resolves to any of the other types during type-checking.
     */
    @Test
    void testSameIsNotAssignableToAnyOtherType() {
        assertIsAssignableTo(type -> assertFalse(ValueType.SAME.isAssignableTo(type)));
    }

    @Test
    void testDateIsNotAssignableToNumber() {
        assertFalse(ValueType.DATE.isAssignableTo(ValueType.NUMBER));
    }

    @Test
    void testNumberIsNotAssignableToDate() {
        assertFalse(ValueType.NUMBER.isAssignableTo(ValueType.DATE));
    }

    /*
    Type Coercion (bit like dynamic types in JS)
     */

    @Test
    void testAnyTypeIsTypeCoercionToString() {
        assertIsAssignableTo(type -> assertTrue(type.isMaybeAssignableTo(ValueType.STRING)));
    }

    @Test
    void testStringIsTypeCoercionToNumber() {
        assertTrue(ValueType.STRING.isMaybeAssignableTo(ValueType.NUMBER));
    }

    @Test
    void testStringIsTypeCoercionToDate() {
        assertTrue(ValueType.STRING.isMaybeAssignableTo(ValueType.DATE));
    }

    @Test
    void testNumberIsNoTypeCoercionToDate() {
        assertFalse(ValueType.NUMBER.isMaybeAssignableTo(ValueType.DATE));
    }


    private static void assertIsAssignableTo(Consumer<ValueType> test) {
        Stream.of(ValueType.values())
                .filter(type -> type != ValueType.SAME)
                .forEach(test);
    }
}
