package org.hisp.dhis.expression.spi;

import java.util.Map;
import java.util.Set;

/**
 * Backend for "loading" data values by an {@link UID} combination.
 *
 * @author Jan Bernitt
 */
@FunctionalInterface
public interface DataItemBackend {

    /**
     * Usually a value is a number but some values may be booleans or strings.
     *
     * @param items UID combinations locating the target data value(s)
     * @return values for each item in no particular order.
     *   Items with a null value may or may not be contained in the result map.
     *   As usual the result map should be considered immutable.
     */
    Map<DataItem, Object> dataValues( Set<DataItem> items );

}
