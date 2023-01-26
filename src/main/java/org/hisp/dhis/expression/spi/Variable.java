package org.hisp.dhis.expression.spi;

import lombok.Value;
import org.hisp.dhis.expression.ast.ProgramVariable;

@Value
public class Variable {

    ProgramVariable name;
    QueryModifiers modifiers;
}
