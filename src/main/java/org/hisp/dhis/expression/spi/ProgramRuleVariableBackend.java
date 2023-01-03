package org.hisp.dhis.expression.spi;

import java.util.Map;
import java.util.Set;

public interface ProgramRuleVariableBackend {

    /**
     *
     * @param names
     * @return
     */
    default Map<String, Object> programRuleVariableValues(Set<String> names) {
        return Map.of();
    }
}
