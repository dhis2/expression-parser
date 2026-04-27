package org.hisp.dhis.lib.expression.spi

internal actual fun matchesPattern(input: String, pattern: String): Boolean =
    input.matches(pattern.toRegex())
