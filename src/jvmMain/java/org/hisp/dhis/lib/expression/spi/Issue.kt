package org.hisp.dhis.lib.expression.spi

data class Issue(
    val position: () -> String,
    val message: String
)
