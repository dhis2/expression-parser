package org.hisp.dhis.expression.eval;

import org.hisp.dhis.expression.ast.DataItemType;
import org.hisp.dhis.expression.ast.UID;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * Backend for "loading" data values by an {@link UID} combination.
 *
 * @author Jan Bernitt
 */
@FunctionalInterface
public interface DataItemBackend {

    /**
     * Loads a value based on a combination of {@link UID}s.
     *
     * Usually a value is a number but some values may be booleans or strings.
     *
     * @param item UID combination locating the target data value(s)
     * @param modifiers lookup modifiers
     * @return the value, maybe null
     */
    Object dataValue(DataItem item, DataItemModifiers modifiers );

    final class DataItem {

        public final DataItemType type;
        public final List<UID> uids;

        public DataItem(DataItemType type, List<UID> uids) {
            this.type = type;
            this.uids = uids;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DataItem) {
                DataItem other = (DataItem) obj;
                        return type == other.type && uids.equals(other.uids);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return type.hashCode() ^ uids.hashCode();
        }

        @Override
        public String toString() {
            return uids.stream().map(UID::getValue).collect(joining(","));
        }
    }

    final class DataItemModifiers {
        public boolean periodAggregation;
        public String aggregationType;
        public LocalDateTime maxDate;
        public LocalDateTime minDate;
        public Integer periodOffset;
        public Integer stageOffset;
    }

   // => DimensionalItemId /*, QueryModifiers modifiers */

    // for aggregation a sequence of data values is returned

    // (#{<UID1>}+#{<UID2>}).aggregationType(SUM)
    //#{<UID1>.<UID2>}.aggregationType(SUM)


    // avg(#{<UID1>}+#{<UID2>}*2)
}
