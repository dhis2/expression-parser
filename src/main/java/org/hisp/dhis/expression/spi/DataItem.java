package org.hisp.dhis.expression.spi;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;
import java.util.function.UnaryOperator;

import static java.util.stream.Collectors.joining;

@Value
@AllArgsConstructor
public class DataItem {

    DataItemType type;
    ID uid0;
    List<ID> uid1;
    List<ID> uid2;
    QueryModifiers modifiers;

    public DataItem(DataItemType type, ID... uid012) {
        this(type,
                uid012[0],
                uid012.length > 1 ? List.of(uid012[1]): List.of(),
                uid012.length > 2 ? List.of(uid012[2]): List.of(),
                QueryModifiers.builder().build() );
    }

    public DataItem(DataItemType type, ID uid0, QueryModifiers modifiers) {
        this(type, uid0, List.of(), List.of(), modifiers );
    }

    public DataItem(DataItemType type, ID uid0, UnaryOperator<QueryModifiers.QueryModifiersBuilder> modifiers) {
        this(type, uid0, modifiers.apply(QueryModifiers.builder()).build());
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(type.getSymbol()).append("{");
        str.append(uid0.toString());
        if (!uid1.isEmpty())
            str.append('.').append( uid1.stream().map(ID::getValue).collect(joining("&")));
        if (!uid2.isEmpty())
            str.append('.').append( uid2.stream().map(ID::getValue).collect(joining("&")));
        str.append("}").append(modifiers);
        return str.toString();
    }
}
