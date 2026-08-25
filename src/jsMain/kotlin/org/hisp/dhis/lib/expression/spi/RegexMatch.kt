package org.hisp.dhis.lib.expression.spi

internal actual fun matchesPattern(input: String, pattern: String): Boolean {
    // Avoid Kotlin stdlib's Regex wrapper which may add the JS `u` flag and reject unknown backslash
    // escapes. Use RegExp directly with ^(?:...)$ anchoring to replicate full-string matching.
    return js("new RegExp('^(?:' + pattern + ')$').test(input)") as Boolean
}
