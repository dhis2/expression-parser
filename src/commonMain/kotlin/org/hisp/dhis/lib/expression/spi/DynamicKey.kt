package org.hisp.dhis.lib.expression.spi

/**
 * Enumeration of dynamic supplementary keys that depend on metadata objects.
 *
 * For example, [DynamicKey.ORG_UNIT_GROUP_SET] represents the UID of an
 * organisation unit group set, which is used by expressions like
 * d2:inOrgUnitGroup().
 *
 * @author Zubair Asghar
 */
enum class DynamicKey {
    ORG_UNIT_GROUP_SET
}