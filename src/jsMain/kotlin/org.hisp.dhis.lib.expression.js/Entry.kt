package org.hisp.dhis.lib.expression.js

@OptIn(ExperimentalJsExport::class)
@JsExport
data class Entry<K,V>(val key: K, val value: V)