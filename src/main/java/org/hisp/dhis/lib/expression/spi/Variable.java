package org.hisp.dhis.lib.expression.spi;

import lombok.Value;
import org.hisp.dhis.lib.expression.ast.ProgramVariable;

@Value
public class Variable {

    ProgramVariable name;
    QueryModifiers modifiers;
}
