package org.hisp.dhis.lib.expression.spi

/**
 * Enumeration of fixed supplementary keys that are always available
 * in the system and do not depend on external metadata.
 *
 * Used to store data like user roles or user groups in expressions
 * (e.g., d2:hasUserRole()).
 *
 * @author Zubair Asghar
 */
enum class FixedKey {
    USER_ROLES,
    USER_GROUPS
}