package org.hisp.dhis.lib.expression.spi

/**
 * An identifier of some type as used in [DataItem]s.
 *
 * Mostly these are UIDs but sometimes these are other identifiers.
 *
 * @author Jan Bernitt
 */
data class ID(
    val type: IDType,
    val value: String
) {
    override fun toString(): String {
        return value
    }
}
