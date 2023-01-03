package org.hisp.dhis.expression.spi;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static java.util.stream.Collectors.joining;

@Value
@AllArgsConstructor
public class DataItem {

    DataItemType type;
    UID uid0;
    List<UID> uid1;
    List<UID> uid2;
    DataItemModifiers modifiers;

    public DataItem(DataItemType type, UID... uid012) {
        this(type,
                uid012[0],
                uid012.length > 1 ? List.of(uid012[1]): List.of(),
                uid012.length > 2 ? List.of(uid012[2]): List.of(),
                DataItemModifiers.builder().build() );
    }

    @Override
    public String toString() {
        String str = uid0.toString();
        if (!uid1.isEmpty())
            str += uid1.stream().map(UID::getValue).collect(joining("&"));
        if (!uid2.isEmpty())
            str += uid2.stream().map(UID::getValue).collect(joining("&"));
        return str;
    }
}
