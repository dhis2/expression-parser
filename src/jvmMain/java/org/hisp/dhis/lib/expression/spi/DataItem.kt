package org.hisp.dhis.lib.expression.spi

import lombok.Value
import java.util.stream.Collectors

@Value
class DataItem(
    var type: DataItemType,
    var uid0: ID,
    var uid1: List<ID>,
    var uid2: List<ID>,
    var modifiers: QueryModifiers) {

    constructor(type: DataItemType, vararg uid012: ID) : this(
        type,
        uid012[0],
        if (uid012.size > 1) listOf<ID>(uid012[1]) else listOf<ID>(),
        if (uid012.size > 2) listOf<ID>(uid012[2]) else listOf<ID>(),
        QueryModifiers()
    )

    constructor(type: DataItemType, uid0: ID, modifiers: QueryModifiers) : this(
        type,
        uid0,
        listOf<ID>(),
        listOf<ID>(),
        modifiers
    )

    fun getKey(): String {
        val diStr = toString()
        return diStr.substring(2, diStr.length - 1)
    }

    override fun toString(): String {
        val str = StringBuilder()
        str.append(type.symbol).append("{")
        str.append(uid0.toString())
        if (uid1.isNotEmpty()) str.append('.').append(uid1.stream().map { obj: ID -> obj.value }
            .collect(Collectors.joining("&")))
        if (uid2.isNotEmpty()) str.append('.').append(uid2.stream().map { obj: ID -> obj.value }
            .collect(Collectors.joining("&")))
        str.append("}").append(modifiers)
        return str.toString()
    }
}
