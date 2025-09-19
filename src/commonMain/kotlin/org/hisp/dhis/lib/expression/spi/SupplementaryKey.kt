package org.hisp.dhis.lib.expression.spi

/**
 * Supplementary keys provide extra contextual data (like user roles, user groups,
 * or membership in organisation unit groups) that cannot be derived from
 * the tracked entity or event data alone.
 *
 * This key can be either [Fixed], for well-defined values for example user roles,
 * or [Dynamic], for metadata-driven keys (e.g., organisation unit group sets).
 *
 * @author Zubair Asghar
 */
sealed class SupplementaryKey {
    data class Fixed(
        val type: FixedKey
    ) : SupplementaryKey()

    data class Dynamic(
        val type: DynamicKey,
        val uid: String
    ) : SupplementaryKey()
}
