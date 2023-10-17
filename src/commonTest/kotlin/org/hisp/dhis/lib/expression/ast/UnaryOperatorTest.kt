package org.hisp.dhis.lib.expression.ast

import kotlin.test.Test
import kotlin.test.assertEquals

internal class UnaryOperatorTest {

    @Test
    fun testNegate_Integer() {
        assertEquals(-42.0, UnaryOperator.negate(42))
    }
    @Test
    fun testNegate_Double() {
        assertEquals(-1.4, UnaryOperator.negate(1.4))
    }
}