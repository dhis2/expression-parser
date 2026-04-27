package org.hisp.dhis.lib.expression.spi

internal expect fun matchesPattern(input: String, pattern: String): Boolean
