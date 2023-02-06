package org.hisp.dhis.lib.expression.spi;

import java.util.List;

/**
 * A variable value as used int he rule-engine.
 */
public interface VariableValue {

    /**
     * @return variable value, maybe null
     */
    String value();

    Object valueOrDefault();

    /**
     * @return list of candidates, never null
     */
    List<String> candidates();

    /**
     * @return associated event date, maybe null
     */
    String eventDate();

    ValueType valueType();
}
