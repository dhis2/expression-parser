package org.hisp.dhis.expression.eval;

import org.hisp.dhis.expression.ast.NamedValue;

/**
 * Backend for all {@link org.hisp.dhis.expression.ast.NamedValue}s.
 *
 * All are implemented as default methods to be overridden by an actual implementation if needed.
 *
 * @author Jan Bernitt
 */
public interface NamedValueBackend {

    default Object namedValue(NamedValue key) {
        return key.name();
    }
}
